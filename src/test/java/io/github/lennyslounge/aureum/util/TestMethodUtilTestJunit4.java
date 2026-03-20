package io.github.lennyslounge.aureum.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMethodUtilTestJunit4 {

    @Rule
    public TestName testMethodName = new TestName();

    @Test
    public void shouldFindMethod(){
        Method currentMethod = TestMethodUtil.findCurrentTestMethod();

        assertThat(currentMethod.getName()).isEqualTo(testMethodName.getMethodName());
    }

    @Test
    public void shouldFindMethodAfterIndirection(){
        Method currentMethod = getCurrentTestMethodIndirect();

        assertThat(currentMethod.getName()).isEqualTo(testMethodName.getMethodName());
    }

    private Method getCurrentTestMethodIndirect(){
        return TestMethodUtil.findCurrentTestMethod();
    }

    @Test
    public void shouldFindMethodInLocalClass(){
        class LocalClass{
            void runTest(){
                Method currentMethod = TestMethodUtil.findCurrentTestMethod();

                assertThat(currentMethod.getName()).isEqualTo(testMethodName.getMethodName());
            }
        }
        new LocalClass().runTest();
    }

    @Test
    public void shouldFindMethodInLambda(){
        Runnable test = () -> {
            Method currentMethod = TestMethodUtil.findCurrentTestMethod();

            assertThat(currentMethod.getName()).isEqualTo(testMethodName.getMethodName());
        };
        test.run();
    }

}
