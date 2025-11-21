package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.bo.conversionBO;

import java.io.BufferedReader;
import java.io.IOException;

//Worker làm xong phải báo cáo server thông qua HTTP
@WebServlet("/api/callback")
public class CallBackServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        // Doc json tu worker gui ve
        StringBuilder sb = new StringBuilder();
        String line;
        try(BufferedReader reader = request.getReader()){
            while( (line = reader.readLine()) != null){
                sb.append(line);
            }
        }
        try{
            // Parse json
            Gson gson = new Gson();
            JsonObject data =  gson.fromJson(sb.toString(), JsonObject.class);

            int id = data.get("id").getAsInt();
            String status =  data.get("status").getAsString();

            conversionBO conversionBO =  new conversionBO();

            if("COMPLETED".equalsIgnoreCase(status)){
                String outputUrl = data.get("output_url").getAsString();
                String outputPublicId = data.get("output_public_id").getAsString();
                //update db thanh cong
                conversionBO.updateStatus(id, outputUrl, outputPublicId);
            }
            else {
                String error = data.has("errorMessage") ? data.get("errorMessage").getAsString() : null;
                //update db that bai
                conversionBO.updateStatus(id, "FAILED", error);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"message\": \"Updated successfully\"}");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

}
