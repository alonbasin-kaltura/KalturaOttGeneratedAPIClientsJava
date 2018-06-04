package com.kaltura.client.test.utils;

import com.google.common.reflect.ClassPath;
import io.qameta.allure.Description;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.Set;

public class InfraUtils {

    public static void writeUtilsListToFile() throws IOException {
        String packageName = "com.kaltura.client.test.utils";

        File file = new File("src/test/resources/utils_list.txt");
        FileUtils.writeStringToFile(file, "package - " + packageName + "\n", Charset.defaultCharset());

        Set<ClassPath.ClassInfo> allClasses = ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses(packageName);
        for (ClassPath.ClassInfo classInfo: allClasses) {
            FileUtils.writeStringToFile(file, "\n" + "class - " + classInfo.getSimpleName() + ":" + "\n", Charset.defaultCharset(), true);

            Class clazz = classInfo.load();
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isStatic(methods[i].getModifiers())) {
                    FileUtils.writeStringToFile(file, methods[i].getName() + "\n", Charset.defaultCharset(), true);
                }
            }
        }
    }

    public static void writeDbUtilsListToFile() throws IOException {
        String packageName = "com.kaltura.client.test.utils.dbUtils";

        File file = new File("src/test/resources/db_utils_list.txt");
        FileUtils.writeStringToFile(file, "package - " + packageName + "\n", Charset.defaultCharset());

        Set<ClassPath.ClassInfo> allClasses = ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClasses(packageName);
        for (ClassPath.ClassInfo classInfo: allClasses) {
            FileUtils.writeStringToFile(file, "\n" + "class - " + classInfo.getSimpleName() + ":" + "\n", Charset.defaultCharset(), true);

            Class clazz = classInfo.load();
            Method[] methods = clazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (Modifier.isStatic(methods[i].getModifiers())) {
                    FileUtils.writeStringToFile(file, methods[i].getName() + "\n", Charset.defaultCharset(), true);
                }
            }
        }
    }

    public static void printTestsNameAndDescription() throws IOException {
        String packageName = "com.kaltura.client.test.tests.servicesTests";

        Set<ClassPath.ClassInfo> allClasses = ClassPath.from(ClassLoader.getSystemClassLoader()).getTopLevelClassesRecursive(packageName);
        for (ClassPath.ClassInfo classInfo: allClasses) {
            System.out.println("\n\n" + classInfo.getSimpleName());

            Class clazz = classInfo.load();
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                Description description = method.getAnnotation(Description.class);
                if (description != null) {
                    System.out.println("Method: " + method.getName());
                    System.out.println("Description: " + description.value() + "\n");
                }
            }
        }
    }
}