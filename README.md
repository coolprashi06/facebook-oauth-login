# facebook-oauth-login

Application demonstrating use of OAuth 2.0 spec to pull the user details from Facebook app.

## Steps:

### Create Facebook app

- go to https://developers.facebook.com/apps/
- Click on **Add a new app** which opens up a window and asks you the type of application which will consume the data from Facebook.
- for this case you need to select Website
- in next screen choose Display name of app, contact email and category of your web application.
- Next page takes you to the dashboard of your newly created application with details like App ID, App Secret

Your facebook app is created and you can copy App ID and Secret from the dashboard for future reference.

### Associate Facebook app with your website

- go to your app on facebook https://developers.facebook.com/apps/
- On left hand side menu, click on Add Product
- Search for Facebook Login and then click on Get Started button. This takes you to configuration of Facebook Login page.
- Under Client OAuth settings, enable Web OAuth Login by using toggle Yes/No button
- Now provide Valid OAuth redirect URIs by specifying following URL: http://{SERVER_URI}/fb_oauth_login/rest/callback.
NOTE: Kindly replace SERVER_URI with the appropriate server URL of your server hosting this application.
- Click on Save Changes

### Configure & Build

- Clone this project onto a folder in your machine.
- Traverse to directory {GIT_REPO}/facebook_oauth_login/src/main/webapps/WEB-INF and open web.xml
- Replace FB_APP_ID and FB_APP_SECRET with the respective values from your facebook app
- Replace SERVER_URI with the appropriate server URL of your server hosting this application. 
- Save web.xml
- go to command-line/terminal and traverse to the directory containing this project
- As this is a maven based project, run following command "mvn clean package" to build the app
- Build will take approx 2-3 minutes to complete and after successful build go to {project_directory}/target where you'll find fb_oauth_login.war file

### Deploy 

- Download any servlet container i.e. JBoss or Tomcat. Assuming here we're using Tomcat as our container.
- Configure it to run on any non-used port i.e. 8383, 8282 etc... I used 8383
- go to {TOMCAT_DIR}/webapps. Here TOMCAT_DIR refers to the directory of apache tomcat.
- Place fb_oauth_login.war file (created during Build step)
- Now go to {TOMCAT_DIR}/bin via command line/terminal and type startup.exe (WIN) or sh startup.sh (MAC)

### Run & Use

- Let's say we're running Tomcat on 8383 port then invoke:
  http://localhost:8383/fb_oauth_login/index.html
- Click on Facebook connect button
- This will prompt you to login with your facebook credentials and then prompts you to authorize this application to get your profile data from facebook.


