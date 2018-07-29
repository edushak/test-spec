package org.edushak.testspec.util

import groovy.json.JsonOutput
import groovy.util.slurpersupport.NodeChild
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse

import javax.net.ssl.SSLException

class RestHelper {

    static HttpResponseDecorator call(String endpoint, String method, Map<String,Object> params, Map headers, authFile = null) {
        RESTClient client = new RESTClient(endpoint)
        if (headers != null && !headers.isEmpty()) {
            client.headers.putAll(headers)
        }
        setCertificates(client, authFile)

        HttpResponseDecorator restResponse
        long start = System.currentTimeMillis(), end
        try {
            restResponse = client."${method.toLowerCase()}"(params.clone())
            end = System.currentTimeMillis()
        } catch (SSLException ssle) {
            throw new Exception("Something went wrong with SSL website", ssle) // TODO: TestSpecException
        } catch (ConnectException ce) {
            throw new Exception("Cannot connect to target URL", ce) // TODO: TestSpecException
        }  catch (HttpResponseException hre) {
            end = System.currentTimeMillis()
            restResponse = hre.response
        }
        restResponse?.metaClass.getPlainText = { -> toString(restResponse) }.memoize()
        restResponse?.metaClass.getResponseTime = (end - start)

        if (!restResponse.success) {
            println """
            |Rest call was unsuccessful:
            |  statusLine: ${restResponse.statusLine}
            |  reason: ${restResponse.data?.error?.reason}
            |  error: ${restResponse.data?.error}
            |Request:
            |  endpoint: ${endpoint}
            |  method: ${method}
            |  headers: ${headers}
            |  parameters: ${params}
            """
        }
        return restResponse
    }

    static Map<String,String> getHeaders(HttpResponse httpResponse) {
        httpResponse.allHeaders.collectEntries { [(it.name):it.value] }
    }

    static String toString(HttpResponse httpResponse) {
        toString(httpResponse?.data)
    }

    static String toString(StringReader response) {
        IOUtils.toString(response)?.trim()
    }

    static String toString(NodeChild node) {
        node?.text()
    }

    static String toString(Map response) {
        JsonOutput.prettyPrint(JsonOutput.toJson(response))
    }

    static String toString(char [] response) {
        new String(response)
    }

    static String toString(String response) {
        response
    }

    static def setCertificates(RESTClient client, def authFileOrPath) {
        if (authFileOrPath) {
            File authFile = new File() // TODO
            if (authFile.exists()) {
                ConfigObject authConfig = Helper.loadConfig(authFile.absolutePath)
                Object jks = authConfig.jks
                if (authConfig.jks) {
                    // could be relative paths
                    File keyStoreFile = Helper.getProjectFile(jks.keyStoreFile)
                    File trustStoreFile = Helper.getProjectFile(jks.trustStoreFile)

                    System.setProperty('javax.net.ssl.trustStore', Helper.getProjectFile(trustStoreFile.toString()))
                    System.setProperty('javax.net.ssl.trustPassword', jks.truststorePassword)
                    client.auth.certificate(keyStoreFile.toURI().toURL().toString(), jks.clientPassword)
                }
            }
        }
    }
}
