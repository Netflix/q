/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.search.query.engine;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.utils.StringUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public abstract class BaseIndexer {
    public static final Logger logger = LoggerFactory.getLogger(BaseIndexer.class);

    public static final String ENCODING = "UTF-8";
    private static final int BUFFER_SIZE = 1 << 16; // 64K
    private Client client = Client.create();

    private String inputFileName=null;
    private String testName=null;

    public BaseIndexer(String inputFileName, String testName) {
        this.inputFileName = inputFileName;
        this.testName = testName;
    }
    
    public void indexData(List<String> languages) throws Throwable
    {
        long start = System.currentTimeMillis();
        List<Map<String, Object>> docs = createDocs(languages);
        update(docs);
        commit();
        logger.info("Indexing took: " + (System.currentTimeMillis() - start) + " ms");
    }

    protected List<Map<String, Object>> createDocs(List<String> languages) throws Throwable
    {
        List<Map<String, Object>> docs = new ArrayList<Map<String, Object>>();

        InputStream is = new BufferedInputStream(new FileInputStream(inputFileName), BUFFER_SIZE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, ENCODING), BUFFER_SIZE);
        String lineString = null;
        while ((lineString = reader.readLine()) != null) {
            String[] line = lineString.split(Properties.inputDelimiter.get());
			if (lineString.startsWith(Properties.idField.get() + Properties.inputDelimiter.get()))
				continue;
            if (line.length < 3)
                logger.error("Bad data: " + lineString);
            else
                docs.add(createDoc(line[0], line[1], line[2], line[3], languages));
        }
        reader.close();
        is.close();
        return docs;
    }

    public Map<String, Object> createDoc(String id, String english, String local, String altTitle, List<String> languages)
    {
        Map<String, Object> doc = new HashMap<String, Object>();
        doc.put(Properties.idField.get(), StringUtils.createIdUsingTestName(id, testName));
        
        for(String requiredField:Properties.requiredNumericFields.get())
        	doc.put(requiredField, 1);

        for(String requiredField:Properties.requiredStringFields.get())
        	doc.put(requiredField, "query_testing_default");

        if (local != null && local.length() > 0) {
            for (String language: languages){
            	for(String fieldName: Properties.titleFields.get())
            		doc.put(fieldName + "_" + language, addValue(doc, language, fieldName, local));
            }
        }
        if (english != null && english.length() > 0) {
        	for(String fieldName: Properties.titleFields.get())
        		doc.put(fieldName+"_en", english);
        }
		if (altTitle != null && altTitle.length() > 0) {
			for (String language : languages) {
				if (Properties.languagesRequiringAdditionalField.get().contains(language))
					for (String fieldName : Properties.titleFields.get()){
						doc.put(fieldName + "_" + language, addValue(doc, language, fieldName, altTitle));
					}
			}
		}

        doc.put(Properties.docTypeFieldName.get(), testName);
        
        return doc;
    }

	private Set<String> addValue(Map<String, Object> doc, String language, String fieldName, String title)
	{
		@SuppressWarnings("unchecked")
		Set<String> existingValues = (Set<String>)doc.get(fieldName + "_" + language);
		if(existingValues==null) existingValues = Sets.newHashSet();
		existingValues.add(title);
		return existingValues;
	}

	private boolean update(List<Map<String, Object>> docs) throws IOException {
		for (Map<String, Object> doc : docs) {
			try {
				addDoc(doc);
			} catch (Throwable e) {
				logger.error("bad doc" + doc);
				throw new RuntimeException(e);
			}
		}
		return true;
	}

	void addDoc(Map<String, Object> doc)
	{
		JsonNode node = new ObjectMapper().valueToTree(doc);
		StringBuilder jsonString = getJsonStringOfDoc(node);

		WebResource webResource = client.resource(getUrlForAddingDoc(doc));
		ClientResponse response = webResource.type("application/json").post(ClientResponse.class, jsonString.toString());
		if (response == null || (response.getStatus() != 201 && response.getStatus() != 200))
		{
			throw new RuntimeException("Failed : HTTP error code on adding a doc: " + response.getStatus());
		}
		response.close();
	}

	public StringBuilder getJsonStringOfDoc(JsonNode node)
	{
		StringBuilder jsonString = new StringBuilder("[");
		nodeAsString(node, jsonString);
		jsonString.append("]");
		return jsonString;
	}

	public void nodeAsString(JsonNode node, StringBuilder jsonString)
	{
		try
		{
			jsonString.append(new ObjectMapper().writeValueAsString(node));
		} catch (JsonProcessingException e)
		{
			logger.error("Error trying to generate a string from a json node", e);
		}
	}

	void commit()
	{
		WebResource webResource = client.resource(getUrlForCommitting());
		ClientResponse response = webResource.get(ClientResponse.class);
		if (response == null || (response.getStatus() != 201 && response.getStatus() != 200))
		{
			throw new RuntimeException("Failed : HTTP error code on commit: " + response.getStatus());
		}
		response.close();
	}
	
    public String getServerUrl(){
    	return "http://" + Properties.engineHost.get() + ":" + Properties.enginePort.get() + "/" + Properties.engineServlet.get() + "/" + Properties.engineIndexName.get();
    }
	
	public abstract String getUrlForAddingDoc(Map<String, Object> doc);
	public abstract String getUrlForCommitting();

    public Map<String, String> getTitleToIds() throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        Map<String, String> titleIdToName = Maps.newHashMap();

        InputStream is = new BufferedInputStream(new FileInputStream(inputFileName), BUFFER_SIZE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, ENCODING), BUFFER_SIZE);
        String lineString = null;
        while ((lineString = reader.readLine()) != null) {
			String[] line = lineString.split(Properties.inputDelimiter.get());
			String id = line[0];
			titleIdToName.put(StringUtils.createIdUsingTestName(id, testName), line[2]);
        }
        reader.close();
        is.close();
        return titleIdToName;
    }
}