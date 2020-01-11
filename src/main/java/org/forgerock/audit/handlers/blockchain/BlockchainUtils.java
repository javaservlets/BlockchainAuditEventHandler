package org.forgerock.audit.handlers.blockchain;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BlockchainUtils {
    Logger logger=LoggerFactory.getLogger(BlockchainUtils.class);
    HttpPost http_post; // we'll use ReST to interface with a Smart Contract already deployed in Kaleido.io
    private static String server="https://(your instance)-connect.us1-azure.kaleido.io";
    private static String key="(your key)"; // used for Basic Auth
    private static String from="(your from value)"; // consortium user is published at this address
    private static String to="(your to value)"; // smart contract is published at this address

    public String postValue(String value) {
        String result="";
        http_post=new HttpPost(server);
        try {
            http_post.setHeader("Authorization", "Basic " + key);
            StringEntity body=new StringEntity("headers:\n  type: SendTransaction\nfrom: " + from + "\nto: " + to + "\nparams:\n  - value: " + value + "\n    type: string\ngas: 1000000\nmethodName: set");
            http_post.setEntity(body);

            HttpClient httpclient=HttpClients.createDefault();
            HttpResponse response=httpclient.execute(http_post);
            HttpEntity entity=response.getEntity();
            HttpEntity responseEntity=response.getEntity();

            if (responseEntity != null) {
                String entity_str=EntityUtils.toString(responseEntity);
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK && entity_str.contains("true")) { //&& entity_str.contains("compliantStatus")
                    logger.info("blockchain hash id: " + entity_str);
                    result=(entity_str);
                } else if (entity_str.contains("not found")) { // do not check httpStatus since srv can throw diff codes n this situation
                    result="unknown";
                }
            } else {
                result="blockchain connection error";
            }
            logger.info(result);
        } catch (IOException e) {
            logger.error(" blockchain http_post e: " + e);
        }
        logger.info("blockchain stored: " + value + " @ " + result);
        return result;
    }

}
