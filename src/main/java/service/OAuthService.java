package service;

import com.google.gson.JsonIOException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;



@Component
@Path("/")
public class OAuthService {

    

    private String generateAppSecretProof(String appSecret, String accessToken){

        String appSecretProof = null;
        System.out.println("Access token "+accessToken);
        System.out.println("App secret token "+appSecret);

        try{
            Mac hmac_sha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(appSecret.getBytes(),"HmacSHA256");
            hmac_sha256.init(secretKeySpec);
            byte[] hashByteArr = hmac_sha256.doFinal(accessToken.getBytes());

            /*
            for (byte b : hashByteArr) {
                System.out.format("%02x", b);
            }
            System.out.println();
            */

            appSecretProof = new String(Hex.encodeHex(hashByteArr));
            System.out.println("App secret proof generated "+appSecretProof);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return appSecretProof;
    }


    private String getFacebookGraphURL(String code, ServletContext context){
        String fbGraphUrl = "";

        String appId = context.getInitParameter("fb_app_id");
        String appSecret = context.getInitParameter("fb_app_secret");
        String redirectUri = context.getInitParameter("redirect_uri");


        try {
            fbGraphUrl = "https://graph.facebook.com/oauth/access_token?"
                    + "client_id=" + appId + "&redirect_uri="
                    + redirectUri
                    + "&client_secret=" + appSecret + "&code=" + code;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fbGraphUrl;
    }


    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public Response getAccessToken(@QueryParam("code")String code, @Context ServletContext context){

        URL fbGraphUrl;
        String fbGraphUrlString;
        URLConnection fbConnection;
        StringBuffer b = null;
        String accessToken = "";
        Response response = null;
        String appSecret = context.getInitParameter("fb_app_secret");

        try{
            fbGraphUrlString = getFacebookGraphURL(code, context);
            System.out.println("FB access token URL: "+fbGraphUrlString);

            fbGraphUrl = new URL(fbGraphUrlString);
            fbConnection = fbGraphUrl.openConnection();
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(fbConnection.getInputStream()));
            String inputLine;
            b = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
                b.append(inputLine + "\n");
            in.close();
            System.out.println("Access token string received from facebook: "+b.toString());
        }catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(b != null){
            String accessTokenStr = b.toString();
                    
            String[] accessTokenArr = accessTokenStr.split("\\&");
            int equalsIndex = accessTokenArr[0].indexOf("=");
            accessToken = accessTokenArr[0].substring(equalsIndex+1);
            String appSecretProof = generateAppSecretProof(appSecret,accessToken);
            Map profile = importFacebookProfile(accessToken, appSecretProof);
            if(profile != null){
                String htmlResponse = getHTMLData(profile);
                response = Response.status(Response.Status.OK).entity(htmlResponse).build();
            }else {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Some exception occurred").build();
            }
        }else {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Some exception occurred").build();
        }
        return response;
    }


    private Map importFacebookProfile(String accessToken, String appSecretProof){

        String graph = null;
        Map profile = null;
        try {
            String g = "https://graph.facebook.com/me?access_token="+accessToken+"&fields=email,name,gender,first_name,last_name,birthday,hometown,link&appsecret_proof="+appSecretProof;
            URL u = new URL(g);
            HttpsURLConnection c = (HttpsURLConnection)u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    c.getInputStream()));
            String inputLine;
            StringBuffer b = new StringBuffer();
            while ((inputLine = in.readLine()) != null)
                b.append(inputLine + "\n");
            in.close();
            graph = b.toString();
            System.out.println("profile object received "+graph);
            profile = getGraphData(graph);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR in getting FB graph data. " + e);
        }
        return profile;
    }

    private Map getGraphData(String fbGraph) {
        Map fbProfile = new HashMap();
        try {
            JSONObject json = new JSONObject(fbGraph);


            fbProfile.put("id", json.get("id"));
            fbProfile.put("first_name", json.get("first_name"));
            fbProfile.put("last_name", json.get("last_name"));

            if(json.has("birthday"))
                fbProfile.put("birthday", json.get("birthday"));

            if (json.has("email"))
                fbProfile.put("email", json.get("email"));

            if (json.has("gender"))
                fbProfile.put("gender", json.get("gender"));

            if (json.has("hometown")) {
                JSONObject hometown = (JSONObject)json.get("hometown");
                fbProfile.put("hometown",hometown.get("name"));
            }

            if(json.has("link"))
                fbProfile.put("user_profile_link",json.get("link"));
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        return fbProfile;
    }

    private String getHTMLData(Map profile){

        StringBuffer out = new StringBuffer();

        out.append("<h1>Sample OAuth Login using Facebook</h1>");
        out.append("<h2>Please find your details pulled from Facebook</h2>");

        out.append("<div>Welcome " + profile.get("first_name"));

        out.append("<div>Your Email: " + profile.get("email"));

        if(profile.get("birthday") != null)
            out.append("<div>Your Birthday: " + profile.get("birthday"));

        if(profile.get("hometown") != null)
            out.append("<div>Your hometown: " + profile.get("hometown"));

        out.append("<div>You're: " + profile.get("gender"));
        out.append("<div>Link to your profile: " + profile.get("user_profile_link"));

        return out.toString();
    }

}
