package kg.socservice.faceRecognition.services;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import kg.socservice.faceRecognition.models.FaceModel;
import kg.socservice.faceRecognition.utils.UtilBase64;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private static final String host = "http://169.254.17.1";

    @SneakyThrows
    @Override
    public List<FaceModel> faceIsapi() {

        List<FaceModel> faces = new ArrayList<>();
        List<FaceModel> newStrangersList = new ArrayList<>();
        List<FaceModel> socFaces = new ArrayList<>();

        String str = getAlphaNumericString(35);
        System.out.println(str);
        Unirest.setTimeouts(0, 0);
        HttpResponse<String> response = Unirest.post(host + "/ISAPI/Intelligent/FDLib/FCSearch")
                .header("Authorization", "Basic YWRtaW46dXNlcjEyMzQ1")
                .header("Content-Type", "application/xml")
                .body("<FCSearchDescription>\r\n  " +
                        "<searchID>" + str + "</searchID>\r\n  " +
                        "<snapStartTime>2018-03-09T00:00:00Z</snapStartTime>\r\n  " +
                        "<snapEndTime>2022-03-09T23:59:59Z</snapEndTime>\r\n  " +
                        "<maxResults>500</maxResults>\r\n  " +
                        "<searchResultPosition>1</searchResultPosition>\r\n" +
                        "</FCSearchDescription>")
                .asString();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        InputSource is;
        try {
            builder = factory.newDocumentBuilder();
            is = new InputSource(new StringReader(response.getBody()));
            Document doc = builder.parse(is);
            NodeList nodes = doc.getElementsByTagName("MatchElement");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    Date snapTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(getValue("snapTime", element));
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh-mm-ss");
                    String strDate = dateFormat.format(snapTime);

                    HttpClient httpClient = HttpClientBuilder.create().build();
                    HttpGet request = new HttpGet(getValue("snapPicURL", element));
                    request.setHeader("Authorization", "Basic YWRtaW46dXNlcjEyMzQ1");
                    MultipartFile multipartFileSnapPicURL = null;
                    try (CloseableHttpResponse response3 = (CloseableHttpResponse) httpClient.execute(request)) {
                        HttpEntity entity = response3.getEntity();
                        InputStream inputStream = entity.getContent();
                        multipartFileSnapPicURL = new MockMultipartFile("snapPicURL" + strDate + ".jpg","temp.jpg","", inputStream);
                        if (entity != null) {

//                            try (FileOutputStream outstream = new FileOutputStream(new File("C:/Users/Abdullo/Documents/ProjectsDirectory/faceRecognition/src/main/resources/static/uploads/snapPicURL" + strDate + ".jpg"))) {
//                                entity.writeTo(outstream);
//                            }
                        }
                    }
                    HttpClient httpClient2 = HttpClientBuilder.create().build();
                    HttpGet request2 = new HttpGet(getValue("facePicURL", element));
                    request2.setHeader("Authorization", "Basic YWRtaW46dXNlcjEyMzQ1");
                    MultipartFile multipartFileFacePicURL = null;
                    try (CloseableHttpResponse response3 = (CloseableHttpResponse) httpClient2.execute(request2)) {
                        HttpEntity entity = response3.getEntity();

                        if (entity != null) {
                            InputStream inputStream = entity.getContent();
                            multipartFileFacePicURL = new MockMultipartFile("facePicURL" + strDate + ".jpg","temp.jpg","", inputStream);

//                            try (FileOutputStream outstream = new FileOutputStream(new File("C:/Users/Abdullo/Documents/ProjectsDirectory/faceRecognition/src/main/resources/static/uploads/facePicURL" + strDate + ".jpg"))) {
//                                entity.writeTo(outstream);
//                            }
                        }
                    }
                    HttpClient httpClient3 = HttpClientBuilder.create().build();
                    HttpGet request3 = new HttpGet(getValue("picURL", element));
                    request3.setHeader("Authorization", "Basic YWRtaW46dXNlcjEyMzQ1");
                    MultipartFile multipartFilePicURL = null;

                    try (CloseableHttpResponse response3 = (CloseableHttpResponse) httpClient3.execute(request3)) {
                        HttpEntity entity = response3.getEntity();
                        if (entity != null) {
                            InputStream inputStream = entity.getContent();
                            multipartFilePicURL = new MockMultipartFile("picURL" + strDate + ".jpg","temp.jpg","", inputStream);
//                            try (FileOutputStream outstream = new FileOutputStream(new File("C:/Users/Abdullo/Documents/ProjectsDirectory/faceRecognition/src/main/resources/static/uploads/picURL" + strDate + ".jpg"))) {
//                                entity.writeTo(outstream);
//                            }
                        }
                    }

                    FaceModel faceModel = FaceModel.builder()
                            .snapPicURL(UtilBase64.encoder(multipartFileSnapPicURL))
                            .snapTime(getValue("snapTime", element))
                            .facePicURL(UtilBase64.encoder(multipartFileFacePicURL))
                            .FDname(getValue("FDname", element))
                            .bornTime(getValue("bornTime", element))
                            .name(getValue("name", element))
                            .picURL(UtilBase64.encoder(multipartFilePicURL))
                            .similarity(getValue("similarity", element))
                            .build();
                    faces.add(faceModel);
                }
            }

            for (FaceModel faceModel : faces) {
                if (Integer.parseInt(faceModel.getSimilarity()) < 50 && faceModel.getFDname().equals("Strangers")) {
                    newStrangersList.add(faceModel);
                } else if(faceModel.getFDname().equals("Strangers") && Integer.parseInt(faceModel.getSimilarity()) > 93){
                    socFaces.add(faceModel);
                }
            }

//            for (FaceModel faceModel : newStrangersList) {
//                String name = faceModel.getFacePicURL().split(".jpg")[0];
//                String content = "<PictureUploadData version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"> <FDID>2</FDID><FaceAppendData version=\"2.0\" xmlns=\"http://www.isapi.org/ver20/XMLSchema\"><name>"+faceModel.getFacePicURL().split(".jpg")[0]+"</name></FaceAppendData></PictureUploadData>";
//                OkHttpClient client = new OkHttpClient().newBuilder()
//                        .build();
//                MediaType mediaType = MediaType.parse("multipart/form-data");
//                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
//                        .addFormDataPart("PictureUploadData", null,
//                                RequestBody.create(MediaType.parse("text/xml"), content.getBytes()))
//                        .addFormDataPart("importImage","/C:/Users/Abdullo/Documents/ProjectsDirectory/faceRecognition/src/main/resources/static/uploads/"+faceModel.getFacePicURL(),
//                                RequestBody.create(MediaType.parse("application/octet-stream"),
//                                        new File("/C:/Users/Abdullo/Documents/ProjectsDirectory/faceRecognition/src/main/resources/static/uploads/"+faceModel.getFacePicURL())))
//                        .build();
//                Request request = new Request.Builder()
//                        .url("http://169.254.17.1/ISAPI/Intelligent/FDLib/pictureUpload")
//                        .method("POST", body)
//                        .addHeader("Content-Type", "multipart/form-data")
//                        .addHeader("Authorization", "Basic YWRtaW46dXNlcjEyMzQ1")
//                        .build();
//                Response responseUploadFile = client.newCall(request).execute();
//            }
            return socFaces;


        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }

        return null;
    }

    String getValue(String tag, Element element) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodes.item(0);
        return node.getNodeValue();
    }

    String getAlphaNumericString(int n) {

        // chose a Character random from this String
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        // create StringBuffer size of AlphaNumericString
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            // generate a random number between
            // 0 to AlphaNumericString variable length
            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            // add Character one by one in end of sb
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}
