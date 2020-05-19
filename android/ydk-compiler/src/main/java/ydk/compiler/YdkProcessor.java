package ydk.compiler;


import com.google.auto.service.AutoService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import static javax.lang.model.element.Modifier.PUBLIC;

import ydk.annotations.YdkModule;

@AutoService(Processor.class)
public final class YdkProcessor extends AbstractProcessor {
    private static final String MODULE_NAME = "moduleName";
    private static final String ROOT_DIR = "rootDir";
    private static final String GENERATED_REACT_MODULE = "generatedReactModule";
    private static final String STRING_TYPE = "java.lang.String";

    private Filer filer;

    private Logger logger;
    private TreeMap<String, Object> nativeModules = new TreeMap<>();
    private Elements elementUtils;
    private Types typeUtils;
    private String moduleName;
    private String rootDir;
    private String generatedReactModule;
    private String packageName;
    private ClassName typeReactApplicationContext = ClassName.get("com.facebook.react.bridge", "ReactApplicationContext");
    private ClassName typeReactContextBaseJavaModule = ClassName.get("com.facebook.react.bridge", "ReactContextBaseJavaModule");
    private ClassName typeReadableMap = ClassName.get("com.facebook.react.bridge", "ReadableMap");
    private ClassName typeReadableArray = ClassName.get("com.facebook.react.bridge", "ReadableArray");
    private ClassName typeReactMethod = ClassName.get("com.facebook.react.bridge", "ReactMethod");
    private ClassName typePromise = ClassName.get("com.facebook.react.bridge", "Promise");
    private ClassName typeReactPackage = ClassName.get("com.facebook.react", "ReactPackage");
    private ClassName typeNativeModule = ClassName.get("com.facebook.react.bridge", "NativeModule");
    private ClassName typeViewManager = ClassName.get("com.facebook.react.uimanager", "ViewManager");
    private ClassName typeReactUtils = ClassName.get("ydk.react", "ReactUtils");
    private ClassName typeYdk = ClassName.get("ydk.core", "Ydk");

    static String toClassCase(String s) {
        String[] parts = s.split("-");
        String camelCaseString = "";
        for (String part : parts) {
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return new HashSet<String>() {{
            this.add(MODULE_NAME);
            this.add(ROOT_DIR);
            this.add(GENERATED_REACT_MODULE);
        }};
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        //初始化我们需要的基础工具
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        logger = new Logger(processingEnv.getMessager());
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(MODULE_NAME);
            rootDir = options.get(ROOT_DIR);
            generatedReactModule = options.getOrDefault(GENERATED_REACT_MODULE, "true");
            packageName = moduleName.replaceAll("-", ".") + ".react";
        }
        logger.info(">>> YdkProcessor init. <<<");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //支持的注解
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(YdkModule.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        if (CollectionUtils.isNotEmpty(annotations)) {
            Set<? extends Element> routeElements = env.getElementsAnnotatedWith(YdkModule.class);
            try {
                logger.info(">>> Found routes, start... <<<");
                parseModules(routeElements);

            } catch (Exception e) {
                logger.error(e);
            }
            return true;
        }


        return false;
    }

    private void parseModules(Set<? extends Element> elements) throws IOException {
        if (!CollectionUtils.isNotEmpty(elements))
            return;

        logger.info(">>> Found ydkmodule, size is " + elements.size() + " <<<");

        for (Element element : elements) {
            YdkModule ydkModule = element.getAnnotation(YdkModule.class);

            if (!ydkModule.export())
                continue;
            String simpleName = element.getSimpleName().toString();
            simpleName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
            TypeName type = TypeName.get(element.asType());
            nativeModules.put(element.getSimpleName() + "Module", type);
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(element.getSimpleName() + "Module")
                    .addModifiers(PUBLIC).superclass(typeReactContextBaseJavaModule);

            classBuilder.addField(typeReactApplicationContext, "reactContext", Modifier.PRIVATE);

            classBuilder.addField(type, simpleName, Modifier.PRIVATE);

            classBuilder.addMethod(MethodSpec.methodBuilder("getName")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addStatement("return $S", element.getSimpleName() + "Module")
                    .returns(String.class)
                    .build());
            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(typeReactApplicationContext, "reactContext")
                    .addParameter(type, simpleName, Modifier.FINAL)
                    .addStatement("super($N)", "reactContext")
                    .addStatement("this.$N = $N", "reactContext", "reactContext")
                    .addStatement("this.$N = $N", simpleName, simpleName)
                    .build();
            classBuilder.addMethod(constructor);

            parseMethods(classBuilder, element, simpleName);

            JavaFile.builder(packageName, classBuilder.build())
                    .build().writeTo(filer);
        }
        parsePackage(elements);
    }

    private void parsePackage(Set<? extends Element> elements) throws IOException {
        if (!CollectionUtils.isNotEmpty(elements))
            return;
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(toClassCase(moduleName) + "Package")
                .addModifiers(PUBLIC).addSuperinterface(typeReactPackage);
        MethodSpec.Builder createNativeModules = MethodSpec.methodBuilder("createNativeModules")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeReactApplicationContext, "reactContext")
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), typeNativeModule));
        StringBuilder sb = new StringBuilder();
        ArrayList<Object> args = new ArrayList<>();
        args.add(Arrays.class);
        for (String key : nativeModules.keySet()) {
            sb.append("\nnew $N(reactContext, $T.getModule($T.class)),");
            args.add(key);
            args.add(typeYdk);
            args.add(nativeModules.get(key));
        }
        String statement = sb.toString();
        if (statement.length() > 0) {
            statement = statement.substring(0, statement.length() - 1);
        }
        createNativeModules.addStatement("return $T.asList(" + statement + ")", args.toArray());
        classBuilder.addMethod(createNativeModules.build());
        classBuilder.addMethod(MethodSpec.methodBuilder("createViewManagers")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeReactApplicationContext, "reactContext")
                .addStatement("return $T.asList()", Arrays.class)
                .returns(ParameterizedTypeName.get(ClassName.get(List.class), typeViewManager))
                .build());

        JavaFile.builder(packageName, classBuilder.build())
                .build().writeTo(filer);
    }

    private void parseMethods(TypeSpec.Builder classBuilder, Element element, String implementVar) {


        for (Element ele : element.getEnclosedElements()) {

            if (ele.getModifiers().contains(Modifier.PUBLIC) && ele instanceof ExecutableElement) {
                ExecutableElement execEle = (ExecutableElement) ele;
                String methodName = execEle.getSimpleName().toString();
                if (execEle.getKind() == ElementKind.CONSTRUCTOR)
                    continue;
                TypeMirror returnType = execEle.getReturnType();

                if (returnType.getKind() != TypeKind.DECLARED) {
                    continue;
                }
                DeclaredType declaredType = (DeclaredType) returnType;
                if (!"io.reactivex.Observable".equals(declaredType.asElement().toString()))
                    continue;
                List<? extends VariableElement> params = execEle.getParameters();
                MethodSpec.Builder methodBuilder = MethodSpec
                        .methodBuilder(methodName)
                        .addAnnotation(typeReactMethod)
                        .addModifiers(Modifier.PUBLIC);
                for (VariableElement varParam : params) {
                    TypeMirror tm = varParam.asType();
                    if (isSubtypeOfType(tm, "android.content.Context"))
                        continue;
                    TypeName paramType = TypeName.get(tm);
                    if (paramType.isPrimitive() || paramType.isBoxedPrimitive() || isSubtypeOfType(tm, STRING_TYPE)) {
                        methodBuilder.addParameter(paramType, varParam.getSimpleName().toString());

                    }else if ("java.util.List<java.lang.Object>".equals(paramType.toString())) {
                        methodBuilder.addParameter(typeReadableArray, varParam.getSimpleName().toString());

                    }
                    else {
                        methodBuilder.addParameter(typeReadableMap, varParam.getSimpleName().toString());
                    }
//                    methodBuilder.addStatement("\"" +paramType.toString()+"\"");
                }

                methodBuilder.addParameter(typePromise, "promise");
                StringBuilder sb = new StringBuilder();
                ArrayList<Object> args = new ArrayList<>();
                sb.append("$T observable = $L.$L(");
                args.add(returnType);
                args.add(implementVar);
                args.add(ele.getSimpleName());
                for (int i = 0; i < params.size(); i++) {
                    VariableElement varParam = params.get(i);
                    TypeMirror tm = varParam.asType();
                    if (isSubtypeOfType(tm, "android.content.Context")) {
                        sb.append("getCurrentActivity()");

                    } else {
                        TypeName paramType = TypeName.get(tm);

                        if (paramType.isPrimitive() || paramType.isBoxedPrimitive() || isSubtypeOfType(tm, STRING_TYPE)) {
                            sb.append(varParam.getSimpleName());
                        } else if ("java.util.Map<java.lang.String, java.lang.Object>".equals(paramType.toString())) {
                            sb.append("$N != null ? $N.toHashMap() : null");
                            args.add(varParam.getSimpleName());
                            args.add(varParam.getSimpleName());
                        } else if ("java.util.List<java.lang.Object>".equals(paramType.toString())) {
                            sb.append("$N != null ? $N.toArrayList() : null");
                            args.add(varParam.getSimpleName());
                            args.add(varParam.getSimpleName());
                        } else {
                            sb.append("$T.mapToObject($N, $T.class)");
                            args.add(typeReactUtils);
                            args.add(varParam.getSimpleName());
                            args.add(tm);
                        }
                    }
                    if (i != params.size() - 1) {
                        sb.append(", ");
                    }

                }

                sb.append(")");
                methodBuilder.addStatement(sb.toString(), args.toArray());

                methodBuilder.addStatement("$T.subscribe(observable, promise)", typeReactUtils);

                classBuilder.addMethod(methodBuilder.build());
            }

        }
    }


    static boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        TypeMirror superType = typeElement.getSuperclass();
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeEqual(TypeMirror typeMirror, String otherType) {
        return otherType.equals(typeMirror.toString());
    }


}
