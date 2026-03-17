package io.github.lennyslounge.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMethodUtilTestJunit5 {

    @Test
    public void shouldFindMethod(TestInfo testInfo){
        Method currentMethod = TestMethodUtil.findCurrentTestMethod();

        assertThat(currentMethod).isEqualTo(testInfo.getTestMethod().get());
    }

    @Test
    public void shouldFindMethodAfterIndirection(TestInfo testInfo){
        Method currentMethod = getCurrentTestMethodIndirect();

        assertThat(currentMethod).isEqualTo(testInfo.getTestMethod().get());
    }

    private Method getCurrentTestMethodIndirect(){
        return TestMethodUtil.findCurrentTestMethod();
    }

    @Test
    public void shouldFindMethodInLocalClass(TestInfo testInfo){
        class LocalClass{
            void runTest(){
                Method currentMethod = TestMethodUtil.findCurrentTestMethod();

                assertThat(currentMethod).isEqualTo(testInfo.getTestMethod().get());
            }
        }
        new LocalClass().runTest();
    }

    @Test
    public void shouldFindMethodInLambda(TestInfo testInfo){
        Runnable test = () -> {
            Method currentMethod = TestMethodUtil.findCurrentTestMethod();

            assertThat(currentMethod).isEqualTo(testInfo.getTestMethod().get());
        };
        test.run();
    }

}
