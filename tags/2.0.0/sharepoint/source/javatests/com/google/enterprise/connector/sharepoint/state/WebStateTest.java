//Copyright (C) 2006 Google Inc.

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package com.google.enterprise.connector.sharepoint.state;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.joda.time.DateTime;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.enterprise.connector.sharepoint.TestConfiguration;
import com.google.enterprise.connector.sharepoint.client.SPConstants;
import com.google.enterprise.connector.sharepoint.client.SharepointClientContext;
import com.google.enterprise.connector.sharepoint.client.Util;
import com.google.enterprise.connector.sharepoint.spiimpl.SPDocument;
import com.google.enterprise.connector.sharepoint.spiimpl.SharepointException;

public class WebStateTest extends TestCase {

	WebState webState;
	SharepointClientContext sharepointClientContext;
	String spURL;
	
	public void setUp() throws Exception {
		System.out.println("\n...Setting Up...");
		System.out.println("Initializing SharepointClientContext ...");
		sharepointClientContext = new SharepointClientContext(TestConfiguration.sharepointUrl, TestConfiguration.domain, 
				  TestConfiguration.username, TestConfiguration.Password, TestConfiguration.googleConnectorWorkDir, 
				  TestConfiguration.includedURls, TestConfiguration.excludedURls, TestConfiguration.mySiteBaseURL, 
				  TestConfiguration.AliasMap, TestConfiguration.feedType);		
		
		assertNotNull(sharepointClientContext);
		System.out.println("Creating test Web State for testing.");
		this.spURL =  sharepointClientContext.getSiteURL();
	
		this.webState = new WebState(sharepointClientContext, this.spURL);
		final SPDocument doc1 = new SPDocument("id1", "url1",new GregorianCalendar(2007, 1, 1), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.METADATA_URL_FEED);
		final SPDocument doc2 = new SPDocument("id2", "url2",new GregorianCalendar(2007, 1, 2), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.METADATA_URL_FEED);
		final SPDocument doc3 = new SPDocument("id3", "url3",new GregorianCalendar(2007, 1, 3), SPConstants.NO_AUTHOR, SPConstants.NO_OBJTYPE, SPConstants.PARENT_WEB_TITLE, SPConstants.CONTENT_FEED,SPConstants.METADATA_URL_FEED);
		
		final DateTime time = Util.parseDate("20080702T140516.411+0000");
		ListState testList = null;
		try {
			testList = this.webState.makeListState("{001-002-003}", time);			
		} catch(final SharepointException spe) {
			System.out.println("Failed to initialize test web state.");
		}
		
		testList.setUrl("http://gdc05.persistent.co.in:8889/");
		testList.updateExtraIDAsAttachment("1","4");
		testList.setType(SPConstants.GENERIC_LIST);
		testList.setExisting(false);
		testList.setBiggestID(1);
		
		final ArrayList<SPDocument> crawlQueueList1 = new ArrayList<SPDocument>();
		crawlQueueList1.add(doc1);
		crawlQueueList1.add(doc2);
		crawlQueueList1.add(doc3);
		testList.setCrawlQueue(crawlQueueList1);		
	}
	
	public void testCompareTo() {
		System.out.println("Testing compareTo()...");
		System.out.println("Creating temporary web state to compare");
		try {
			final WebState ws1 = this.webState = new WebState(this.sharepointClientContext, this.spURL);
			final int i = this.webState.compareTo(ws1);
			assertEquals(i, 0);
			System.out.println("[ compareTo() ] Test completed.");
		} catch(final Exception e) {
			System.out.println("[ compareTo() ] Test Failed.");
		}
	}
	
	public void testDocToDOMToDoc() {
		System.out.println("Testing Basic conversion from DOM to DOC and Vice Versa ...");
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		org.w3c.dom.Document doc = null;
		Node node = null;
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.newDocument();
			node = this.webState.dumpToDOM(doc);
			assertNotNull(node);			
		} catch (final ParserConfigurationException e) {
			System.out.println("Unable to get state XML");
			return;
		} catch (final SharepointException spe) {
			System.out.println("[ dumpToDOM() ] Test Failed.");
			return;
		}
		
		try {
			final WebState tempws = new WebState(this.sharepointClientContext, this.spURL);
			tempws.loadFromDOM((Element)node);
			final int i = this.webState.compareTo(tempws);
			assertEquals(i, 0);
			System.out.println("[ Basic conversion from DOM to DOC and Vice Versa ] Test Passed.");
		} catch(final SharepointException spe) {
			System.out.println("[ loadFromDOM() ] Test Failed.");
		}
		
	}
	
	public void testEndRecrawl() {
		System.out.println("Testing endRecrawl for WebState..");
		this.webState.endRecrawl(this.sharepointClientContext);
		System.out.println("[ endRecrawl() ] Test Completed. ");
	}
}
