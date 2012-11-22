package org.fastcode.type;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class GenuineTypeTest {

    public static interface ThrowerContent<E extends Exception, F extends Exception, T> {
        void setT(T t) throws E, F;
    }

    public static interface Content<T> {
        T getT();
    }

    public static interface Content2<U, V> {

        U getU();

        V getV();

        void setU(U u);
    }

    public static interface Content3<T, U, V, E extends Exception> extends Content, Content2, ThrowerContent<E, IOException, T> {
    }

    public static interface Simple<X, Y> extends Content3<Map<String, X>, Y, Set<Integer>, InterruptedException> {
    }

    public static abstract class GType<T> extends GenuineType<T> {
    }

    @Test
    public void testType() throws Exception {
        GenuineType<?> type = new GType<Simple<Map<String, Set<Integer>>, Set<Map<String, Integer>>>>() {};

        System.out.println(type.getType());

        GenuineType<?> returnType = type.getReturnType(Simple.class.getMethod("getT"));
        System.out.println(returnType);

        System.out.println(returnType.getReturnType(returnType.getRawClass().getMethod("get", new Class<?>[]{Object.class})));

        System.out.println(type.getParameterTypes(Simple.class.getMethod("setU", new Class<?>[]{Object.class})));

        System.out.println(type.getExceptionTypes(Simple.class.getMethod("setT", new Class<?>[]{Object.class})));


//        System.out.println(type.getExceptionTypes(Simple.class.getMethod("setT", new Class<?>[]{Object.class})));

//        for (Method method : type.getRawClass().getMethods()) {
//            System.out.println(method);
//            System.out.println(method.getGenericReturnType());
//        }
//
//        Method method = Content2.class.getMethod("getT");
//
//        System.out.println(type.getReturnType(method));
//
//        System.out.println(new GenuineType<Simple>() {}.getType());
    }
}
