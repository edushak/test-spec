package org.edushak.testspec.util

import groovy.util.logging.Slf4j
import groovyx.gpars.actor.DynamicDispatchActor
import org.edushak.testspec.Scenario

@Slf4j
@Singleton
class ElasticClient extends DynamicDispatchActor {

    static final String baseUrl, runId
    static final Map<String,Object> publishable
    static {
        baseUrl = '' // read from config
        runId = InetAddress.localHost.hostName + Date.newInstance().format('-YYYYMMdd-HHmmss')
        publishable = [
            // from config
            environment : '',
            run_type : '',
            run_id : runId,
            suite_name : '',
            // svn_revision : ?
            // test_spec_version : ''
            by_user : '',
            host_name : '',
            host_os : ''
        ]
        //
        /*
        addShutdownHook {
            println """
            Test evidence published to: ...
            """
        }
        */
    }

    void onMessage(final Scenario scenario) {
        // TODO: finish
/*
        Map<String,Object> params = [:]
        Map headers = [:]
        def authFile = null
        RestHelper.call(baseUrl, 'POST', params, headers, authFile)
*/
    }
}
