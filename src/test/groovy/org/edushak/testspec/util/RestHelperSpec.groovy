package org.edushak.testspec.util

import groovyx.net.http.HttpResponseDecorator
import org.edushak.testspec.BaseSpec
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Unroll

class RestHelperSpec extends BaseSpec {

    @Shared accessToken = '1b0ba10b11262698e2ccff05889e8e10006be798'
    @Shared requestHeaders = [
        'Authorization': "token $accessToken",
        'Accept'       : 'application/vnd.github.v3.full+json'
    ]

    @Ignore
    @Unroll
    def "call(#endpoint, #expectedStatus)"(String endpoint, Map headers, expectedStatus, expectedStatusLine) {
        when:
        HttpResponseDecorator response = RestHelper.call(endpoint, 'GET', [:], headers)

        then:
        response.status == expectedStatus
        response.statusLine == expectedStatusLine

        where:
        endpoint                                                         | headers        | expectedStatus | expectedStatusLine
        "https://api.github.com/users/edushak?access_token=$accessToken" | requestHeaders | 200            | '' // return HTTP/1.0 403 Forbidden

        // not a JSON, parsing fails:
        // 'http://crbsrv.jsfiddle.net/ads/CKYIEKQ7.json?segment=placement:jsfiddlenet&callback=_carbonads_go' | 200            | 'bla'
        // 'https://api.github.com/users/eugenp'  | headers | 403            | 'HTTP/1.0 403 Forbidden'
    }
}
/*
REST call to 'https://api.github.com/users/edushak' returns something like:
{
    "login": "edushak",
    "id": 1640491,
    "node_id": "MDQ6VXNlcjE2NDA0OTE=",
    "avatar_url": "https://avatars3.githubusercontent.com/u/1640491?v=4",
    "gravatar_id": "",
    "url": "https://api.github.com/users/edushak",
    "html_url": "https://github.com/edushak",
    "followers_url": "https://api.github.com/users/edushak/followers",
    "following_url": "https://api.github.com/users/edushak/following{/other_user}",
    "gists_url": "https://api.github.com/users/edushak/gists{/gist_id}",
    "starred_url": "https://api.github.com/users/edushak/starred{/owner}{/repo}",
    "subscriptions_url": "https://api.github.com/users/edushak/subscriptions",
    "organizations_url": "https://api.github.com/users/edushak/orgs",
    "repos_url": "https://api.github.com/users/edushak/repos",
    "events_url": "https://api.github.com/users/edushak/events{/privacy}",
    "received_events_url": "https://api.github.com/users/edushak/received_events",
    "type": "User",
    "site_admin": false,
    "name": null,
    "company": null,
    "blog": "",
    "location": null,
    "email": null,
    "hireable": null,
    "bio": null,
    "public_repos": 4,
    "public_gists": 0,
    "followers": 1,
    "following": 4,
    "created_at": "2012-04-13T14:12:34Z",
    "updated_at": "2018-07-29T00:38:53Z"
}
*/
