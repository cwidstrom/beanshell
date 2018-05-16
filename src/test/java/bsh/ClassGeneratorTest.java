/*****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one                *
 * or more contributor license agreements.  See the NOTICE file              *
 * distributed with this work for additional information                     *
 * regarding copyright ownership.  The ASF licenses this file                *
 * to you under the Apache License, Version 2.0 (the                         *
 * "License"); you may not use this file except in compliance                *
 * with the License.  You may obtain a copy of the License at                *
 *                                                                           *
 *     http://www.apache.org/licenses/LICENSE-2.0                            *
 *                                                                           *
 * Unless required by applicable law or agreed to in writing,                *
 * software distributed under the License is distributed on an               *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY                    *
 * KIND, either express or implied.  See the License for the                 *
 * specific language governing permissions and limitations                   *
 * under the License.                                                        *
 *                                                                           *
/****************************************************************************/

package bsh;

import static bsh.TestUtil.eval;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.function.IntSupplier;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

@RunWith(FilteredTestRunner.class)
public class ClassGeneratorTest {
    @Test
    public void create_class_with_default_constructor() throws Exception {
        eval("class X1 {}");
    }

    @Test
    public void creating_class_should_not_set_accessibility() throws Exception {
        boolean current = Capabilities.haveAccessibility();
        Capabilities.setAccessibility(false);
        assertFalse("pre: no accessibility should be set", Capabilities.haveAccessibility());
        TestUtil.eval("class X1 {}");
        assertFalse("post: no accessibility should be set", Capabilities.haveAccessibility());
        Capabilities.setAccessibility(current);
    }

    @Test
    public void create_instance() throws Exception {
        assertNotNull(
            eval(
                "class X2 {}",
                "return new X2();"
        ));
    }


    @Test
    public void constructor_args() throws Exception {
        final Object[] oa = (Object[]) eval(
            "class X3 implements IntSupplier {",
                "final Object _instanceVar;",
                "public X3(Object arg) { _instanceVar = arg; }",
                "public int getAsInt() { return _instanceVar; }",
            "}",
            "return new Object[] { new X3(0), new X3(1) } ");
        assertEquals(0, ( (IntSupplier) oa[0] ).getAsInt());
        assertEquals(1, ( (IntSupplier) oa[1] ).getAsInt());
    }


    @Test
    public void call_protected_constructor_from_script() throws Exception {
        final Object[] oa = (Object[]) TestUtil.eval(
            "class X4 implements java.util.concurrent.Callable {",
                "final Object _instanceVar;",
                "X4(Object arg) { _instanceVar = arg; }",
                "public Object call() { return _instanceVar; }",
            "}",
            "return new Object[] { new X4(0), new X4(1) } ");
        assertEquals(0, ( (Callable) oa[0] ).call());
        assertEquals(1, ( (Callable) oa[1] ).call());
    }


    @Test
    public void verify_public_accesible_modifiers() throws Exception {
        TestUtil.cleanUp();
        boolean current = Capabilities.haveAccessibility();
        Capabilities.setAccessibility(false);

        Class<?> cls = (Class<?>) TestUtil.eval(
            "class X6 {",
                "public Object public_var;",
                "private Object private_var = null;",
                "protected Object protected_var = null;",
                "public final Object public_final_var;",
                "final Object final_var;",
                "static Object static_var;",
                "static final Object static_final_var = null;",
                "volatile Object volatile_var;",
                "transient Object transient_var;",
                "no_type_var = 0;",
                "Object no_modifier_var;",
                "X6() {}",
                "just_method() {}",
                "void void_method() {}",
                "Object type_method() {}",
                "synchronized sync_method() {}",
                "final final_method() {}",
                "static static_method() {}",
                "static final static_final_method() {}",
                "abstract abstract_method() {}",
                "public public_method() {}",
                "private private_method() {}",
                "protected protected_method() {}",
            "}",
            "return X6.class ");

        // public class
        assertTrue("class has public modifier", Reflect.getClassModifiers(cls).hasModifier("public"));

        // public static variables
        assertTrue("static_var has public modifier", var(cls, "static_var", "public"));
        assertTrue("static_var has static modifier", var(cls, "static_var", "static"));
        assertTrue("static_final_var has public modifier", var(cls, "static_final_var", "public"));
        assertTrue("static_final_var has static modifier", var(cls, "static_final_var", "static"));
        assertTrue("static_final_var has final modifier", var(cls, "static_final_var", "final"));

        // public static methods
        assertTrue("static_method has public modifier", meth(cls, "static_method", "public"));
        assertTrue("static_method has static modifier", meth(cls, "static_method", "static"));
        assertTrue("static_final_method has public modifier", meth(cls, "static_final_method", "public"));
        assertTrue("static_final_method has static modifier", meth(cls, "static_final_method", "static"));
        assertTrue("static_final_method has final modifier", meth(cls, "static_final_method", "final"));

        // public instance variables
        assertTrue("public_var has public modifier", var(cls, "public_var", "public"));
        assertFalse("private_var does not have public modifier", var(cls, "private_var", "public"));
        assertTrue("private_var has private modifier", var(cls, "private_var", "private"));
        assertFalse("protected_var does not have public modifier", var(cls, "protected_var", "public"));
        assertTrue("protected_var has protected modifier", var(cls, "protected_var", "protected"));
        assertTrue("public_final_var has public modifier", var(cls, "public_final_var", "public"));
        assertTrue("public_final_var has final modifier", var(cls, "public_final_var", "final"));
        assertTrue("final_var has public modifier", var(cls, "final_var", "public"));
        assertTrue("final_var has final modifier", var(cls, "final_var", "final"));
        assertTrue("transient_var has public modifier", var(cls, "transient_var", "public"));
        assertTrue("transient_var has transient modifier", var(cls, "transient_var", "transient"));
        assertTrue("volatile_var has public modifier", var(cls, "volatile_var", "public"));
        assertTrue("volatile_var has volatile modifier", var(cls, "volatile_var", "volatile"));
        assertTrue("no_modifier_var has public modifier", var(cls, "no_modifier_var", "public"));
        assertTrue("no_type_var has public modifier", var(cls, "no_type_var", "public"));

        // public instance methods
        assertTrue("constructor has public modifier", meth(cls, "X6", "public"));
        assertTrue("just_method has public modifier", meth(cls, "just_method", "public"));
        assertTrue("void_method has public modifier", meth(cls, "void_method", "public"));
        assertTrue("type_method has public modifier", meth(cls, "type_method", "public"));
        assertTrue("sync_method has public modifier", meth(cls, "sync_method", "public"));
        assertTrue("sync_method has synchronized modifier", meth(cls, "sync_method", "synchronized"));
        assertTrue("final_method has public modifier", meth(cls, "final_method", "public"));
        assertTrue("final_method has final modifier", meth(cls, "final_method", "final"));
        assertTrue("abstract_method has public modifier", meth(cls, "abstract_method", "public"));
        assertTrue("abstract_method has abstract modifier", meth(cls, "abstract_method", "abstract"));
        assertTrue("public_method has public modifier", meth(cls, "public_method", "public"));
        assertFalse("private_method does not have public modifier", meth(cls, "private_method", "public"));
        assertTrue("private_method has private modifier", meth(cls, "private_method", "private"));
        assertFalse("protected_method does not have public modifier", meth(cls, "protected_method", "public"));
        assertTrue("protected_method has protected modifier", meth(cls, "protected_method", "protected"));

        Capabilities.setAccessibility(current);
    }

    private boolean var(Class<?> type, String var, String mod) throws UtilEvalError {
        Variable v = Reflect.getDeclaredVariable(type, var);
        return null != v && v.hasModifier(mod);
    }

    private boolean meth(Class<?> type, String meth, String mod) throws UtilEvalError {
        BshMethod m = Reflect.getDeclaredMethod(type, meth, new Class<?>[0]);
        return null != m && m.hasModifier(mod);
    }

    @Test
    public void outer_namespace_visibility() throws Exception {
        final IntSupplier supplier = (IntSupplier) eval(
            "class X4 implements IntSupplier {",
                "public int getAsInt() { return var; }",
            "}",
            "var = 0;",
            "a = new X4();",
            "var = 1;",
            "return a;");
        assertEquals(1, supplier.getAsInt());
    }


    @Test
    public void static_fields_should_be_frozen() throws Exception {
        final IntSupplier supplier =  (IntSupplier)eval(
                "var = 0;",
                "class X5 implements IntSupplier {",
                    "static final Object VAR = var;",
                    "public int getAsInt() { return VAR; }",
                "}",
                "var = 1;",
                "a = new X5();",
                "var = 2;",
                "return a;"
        );
        assertEquals(0, supplier.getAsInt());
    }


    /**
     * See also failing test script "classinterf1.bsh" and
     * <a href="http://code.google.com/p/beanshell2/issues/detail?id=46">issue #46</a>.
     */
    @Test
    @Category(KnownIssue.class)
    public void define_interface_with_constants() throws Exception {
        // these three are treated equal in java
        eval("interface Test { public static final int x = 1; }");
        eval("interface Test { static final int x = 1; }");
        eval("interface Test { final int x = 1; }");
        // these three are treated equal in java
        eval("interface Test { public static int x = 1; }");
        eval("interface Test { static int x = 1; }");
        eval("interface Test { int x = 1; }");
    }
}
