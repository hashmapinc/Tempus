import com.hashmapinc.kubeless.InputParserUtility;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class InputParserUtilityTest {
    @Test
    public void validateCorrectJson() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr),true);
        } catch (IOException e) {

        }
    }

    @Test
    public void validateInCorrectJsonThrowsException() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676}";
        try {
            inputParserUtility.validateJson(jsonStr);
            Assert.fail();
        } catch (IOException e) {
            Assert.assertTrue(true);
            System.out.println("Error occured " + e.getMessage());
        }
    }

    @Test
    public void validateCorrectJsonWithDsAndTs() {
        InputParserUtility inputParserUtility = new InputParserUtility();
        String jsonStr = "{\"id\":\"1\", \"ts\":\"45676\", \"ds\":\"3000\"}";
        try {
            Assert.assertEquals(inputParserUtility.validateJson(jsonStr),false);
        } catch (IOException e) {

        }
    }
}
