import io.cucumber.messages.internal.com.google.common.io.Resources;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class groceryApiTest {

    public String searchProduct = "apple";
    JSONObject json;
    Response response;

    @BeforeMethod
    public void setUp(){
        RestAssured.baseURI = "http://13.48.148.99:90/api/grocery";
    }

    //Bütün ürünlerin gelmesini kontrol ettim.
    @Test
    public void getAllGrocery(){
        response = RestAssured.given()
                .get("/allGrocery")
                .then()
                .statusCode(200)
                .extract().response();

        Assert.assertNotNull(response.getBody().jsonPath().getList("id").get(0));
    }

    //Apple ürününlerinin listelenmesi
    @Test
    public void getGrocerySearchForApple(){
        response = getAllGrocery(searchProduct);
        List<String> product = new ArrayList<>();
        product = response.getBody().jsonPath().getList("name");
        for (String i : product){
            Assert.assertEquals(i.toString(), searchProduct);
        }
    }

    //Apimize ürün ekleme işlemimnin yapıldığının kontrolünün yapılması
    @Test
    public void postGroceryProduct() throws IOException {
        getJsonObject();
        json.put("id", idGenerate());
        json.put("name",searchProduct);
        json.put("price", 20);
        json.put("stock",100);
        addMethod(json);
        Response response = getAllGrocery(idGenerate());
        Assert.assertNotNull(response.getBody().jsonPath().getList("id"));
    }

    //Aynı id'li ürün eklendiğinde 500 statüs code dönmesinin kontrolü
    @Test
    public void postGroceryProductDuplicate() throws IOException {
        getJsonObject();
        json.put("id", 1);
        given()
                .contentType("application/json")
                .body(json.toString())
                .when()
                .post("/Add")
                .then()
                .statusCode(500);
    }

    //apiye ürün ekleme methodu
    public void addMethod(Object json){
        given()
                .contentType("application/json")
                .body(json.toString())
                .when()
                .post("/Add")
                .then()
                .statusCode(200);
    }

    //id verilen ürünlerin çıkartılması methodu
    public Response getAllGrocery(int id){
        return  RestAssured.given()
                .queryParam("id",id)
                .get("/allGrocery")
                .then()
                .statusCode(200)
                .extract().response();
    }

    //name verilen ürünlerin çıkartılması methodu
    public Response getAllGrocery(String value){
        return RestAssured.given()
                .queryParam("name",value)
                .get("/allGrocery")
                .then()
                .statusCode(200)
                .extract().response();
    }

    //duplicate olmayan id üretme methodu
    public int idGenerate(){
        Response response = RestAssured.given()
                .get("/allGrocery").then().extract().response();
        List<Integer> id = response.getBody().jsonPath().getList("id");
        return id.size() + 3;
    }

    //json dosyasını oluşturma işlemi
    public void getJsonObject() throws IOException {
        URL file = Resources.getResource("products.json");
        String myJson = Resources.toString(file, Charset.defaultCharset());
        json = new JSONObject( myJson );
    }

}

