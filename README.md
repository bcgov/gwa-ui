# Kong Api Gateway Administration

<img src="https://github.com/bcgov/gwa-ui/workflows/Build/badge.svg"></img>
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=gwa-ui&metric=alert_status)](https://sonarcloud.io/dashboard?id=gwa-ui)


This project contains a web interface for managing the KONG Api Gateway.

## Installation

### Requirements

1. Kong 0.10.0+
2. luarocks
3. JDK 1.8+
4. J2EE Servlet Container 3.1
  * Jetty 9.4+
  * Tomcat 8.5

### BCGOV GWA Endpoint Kong Plugin

Release and install the kong-bcgov-gwa-endpoint.

[kong-bcgov-gwa-endpoint Release and Installation Instructions](https://gogs.data.gov.bc.ca/DataBC/kong-bcgov-gwa-endpoint)

### Additional Kong Plugins

Install the following additional Kong Plugins

* [Upstream Auth Basic](https://revolsys.github.io/kong-plugin-upstream-auth-basic/)

### GitHub Organization Access

The GWA Developer Key application requires OAuth application access to the organization (bcgov-keygen)
that the developers must be a member of.

1. Open the OAuth applications configuration page for the GitHub organization
     https://github.com/organizations/gwa-qa/settings/applications - Delivery & Test
     https://github.com/organizations/bcgov/settings/applications - Production
2. Click Register a new application and Enter the following fields.
  * Application name: gwa-(prod,test,delivery)
  * **Homepage URL:** https://gwa(-t,-d).apps.gov.bc.ca
  * **Application Description:** GWA Developer key for <environment>
  * **Authorization callback URL:** https://gwa(-t,-d).apps.gov.bc.ca/git/callback
3. Click Register application
4. Record the Client ID and Client Secret for the configuration.

### GitHub Personal Access Token

1. Create a new http://github.com user account that will only be used for managing organization (bcgov-keygen) access.
2. Add the new user as an admin for the organization (bcgov-keygen).
3. Login as the new user.
4. Using https://github.com/settings/tokens generate a new personal access token with the admin:org permission.
5. Record the generated token for use in the gwaGitHubAccessToken configuration.

### Configuration Properties
The application supports the following configuration properties.

| Name                  | Description                | Sample Value                                 |
|-----------------------|----------------------------|----------------------------------------------|
| gwaTermsUrl           | URL to terms of us page    | http://www2.gov.bc.ca/gov/content?id=D1EE0A405E584363B205CD4353E02C88 |
| gwaKongAdminUrl       | URL to kong rest API       | http://localhost:8001                        |
| gwaKongAdminUsername  | Username for kong rest API | kong                                         |
| gwaKongAdminPassword  | Password for kong rest API | dgdftyftye                                   |
| gwaGitHubOrganization | GitHub Org name            | bcgov-keygen                                 |
| gwaGitHubClientId     | GitHub Org Client ID       | abcdefgh0123456789123                        |
| gwaGitHubClientSecret | GitHub Ord Client Secret   | abcdefgh01234567890123456789012345678912     |
| gwaGitHubAccessToken  | GitHub Personal Access Token of Organization Owner | abcdefgh01234567890123456789012345678912     |
| gwaApiKeyExpiryDays   | Number of days before developer API keys expire | 90                      |

Create the config/gwa.properties file containing the above properties. This directory must be in
the working directory that the servlet container runs in.

### Deploy Web Application

|||
|---------------------|-----------------------------|
| **Container**       | Dedicated                   |
| **Context Path**    | /                           |
| **Application URL** | https://gwa.apps.gov.bc.ca/ |


### Application URLS

* https://gwa.apps.gov.bc.ca/     - Developer Keys
* https://gwa.apps.gov.bc.ca/int/ - Gateway (Kong) Administration

### Configure Admin Users

The first time the application is deployed Kong must be configured to allow access for the first
admin user. Further users can be added using the GWA.

Access Gateway Admin URL in a web browser and login using your IDIR account.

https://gwa.apps.gov.bc.ca/int/

This will create a kong consumer for your account.

The consumer username will be all lower case in the form idir_<accountname> (e.g. idir_pxaustin).

Add the consumer to the gwa_admin group.

```bash
curl -X POST http://localhost:8001/consumers/idir_<accountName>/acls \
    --data "group=gwa_admin"
```

Wait 2 minutes for the cache of users groups to clear then access the Gateway Admin. You should now
be granted access.

https://gwa.apps.gov.bc.ca/int/

## Embed API Key in Authorized Applications

Applications such as the API Console can request access to a user's API key so that they can use it to access APIs.

The applications request the API key via the following page and parameters. The user will be required to login to GitHub and authorize access for the application to their API key.

https://gwa.apps.gov.bc.ca/ui/apiKeys

|Parameter|Description|Example|
|---------|-----------|-------|
| appName     | The REQUIRED name of the application that is requesting access to the user's API key. | API Console |
| appRedirectUrl | The URL that the user's web browser will be redirected to when the Authorize Application button is clicked. | https://appConsole.apps.gov.bc.ca/authorizedApiKey |
| appSendMessage | Flag indicating if the API key should be sent via JavaScript window.postMessage. | true |
| contentOnly | Flag indicating if the header and footer should be hidden. | true |

NOTE: If both appSendMessage and appRedirectUrl are specified then the appSendMessage will be used. 

### JavaScript Post Message

Web browsers provide the [window.postMessage](https://developer.mozilla.org/en-US/docs/Web/API/Window/postMessage) method to allow a web page to send messages to other web pages.

The GWA Developer Key page uses this method to send the API key int the message.data field when the user clicks the Authorize Application button. The page must be opened in either an iframe or frame.

The application requesting the API key will add an event listener for messages sent to their window. The following code fragment shows how to register the event listener and get the API key.

```javascript
window.addEventListener('message', function(message) {
  $('#apiKeyModal').modal('hide');
  var apiKey = message.data;
  alert(apiKey);
});
```

The following code shows an example using a bootstrap dialog to get the API key.

```html
<html>
  <head>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
    <script src="https://unpkg.com/jquery@3.2.1" type="text/javascript"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
  </head>
  <body>

  <button type="button" class="btn btn-primary btn-lg" onClick="getApiKey()">
    Get API Key
  </button>

  <div class="modal fade" id="apiKeyModal" tabindex="-1" role="dialog" aria-labelledby="apiKeyModalLabel">
    <div class="modal-dialog modal-lg" role="document">
      <div class="modal-content">
        <div class="modal-header">
          <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
          <h4 class="modal-title" id="apiKeyModalLabel">Modal title</h4>
        </div>
        <div class="modal-body">
          <iframe
            id="apiKeyFrame"
            src="about:blank"
            style="width: 100%; height: 600px; border: 0px"
          >
          </iframe>
        </div>
      </div>
    </div>
  </div>
  </body>
  <script type="text/javascript">
function getApiKey() {
      $('#apiKeyModal').modal('show');
      $('#apiKeyFrame').attr('src', 'https://gwa.apps.gov.bc.ca/ui/apiKeys?appName=Test&appSendMessage=true&contentOnly=true');
}

    window.addEventListener('message', function(message) {
      $('#apiKeyModal').modal('hide');
      var apiKey = message.data;
      alert(apiKey);
    });
  </script>
</html>
```

### Redirect URL

The alternate method is to include the appRedirectUrl with a URL to your application that will be called with the apiKey as a query string parameter when the user clicks the Authorize Application button.

This method does not require the page to be in an iframe or frame.

# Development

`Netbeans` provides quite a good development environment with Tomcat.  VSCode and Intellij both had issues with this project.

```

```

# License
```
Copyright 2018 Province of British Columbia

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
