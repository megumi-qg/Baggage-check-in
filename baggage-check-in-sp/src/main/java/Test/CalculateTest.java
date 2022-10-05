package Test;

import com.qigao.baggagecheckinsp.controller.Calc;
import com.alibaba.fastjson.JSON;
import com.qigao.baggagecheckinsp.entity.Form;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class CalculateTest {

    @Test
    public void UnitTest(List<Form> list) {
        Calc calc = new Calc();
        int res = 0;
        for (int i=0;i<list.size();i++) {
            res = calc.calculatePrice(list.get(i));
            assertEquals("测试成功", res,list.get(i).getRes());
            System.out.println("第"+(i+1)+"项测试通过");
        }
    }
    @Test
    public void WhiteTestout() throws IOException {
        String filePath = "src/main/java/Test/白盒测试-国际航班.json";
        String jsonContent = FileUtil(filePath);
        List<Form> list = JSON.parseArray(jsonContent, Form.class);
        UnitTest(list);
    }

    @Test
    public void BlackTest() throws IOException {
        String filePath = "src/main/java/Test/黑盒测试.json";
        String jsonContent = FileUtil(filePath);
        List<Form> list = JSON.parseArray(jsonContent, Form.class);
        UnitTest(list);
    }
    @Test
    public void WhiteTestin() throws IOException {
        String filePath = "src/main/java/Test/白盒测试-国内航班.json";
        String jsonContent = FileUtil(filePath);
        List<Form> list = JSON.parseArray(jsonContent, Form.class);
        UnitTest(list);
    }

    public static String FileUtil(String Path) throws IOException {
        BufferedReader reader = null;
        StringBuilder laststr = new StringBuilder();
        FileInputStream fileInputStream = new FileInputStream(Path);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        reader = new BufferedReader(inputStreamReader);
        String tempString = null;
        while ((tempString = reader.readLine()) != null) {
            laststr.append(tempString);
        }
        reader.close();
        return laststr.toString();
    }
}
