package com.aopmeta.easybinding.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class BindingProcessor extends AbstractProcessor {
    //BindingCallbacks类全路径
    public final static String CLASS_BINDING_CALLBACKS = "com.aopmeta.easybinding.base.BindingCallbacks";
    //OnBindingCallback类全路径
    public final static String CLASS_CALLBACK_LISTENER = "com.aopmeta.easybinding.base.OnBindingCallback";
    //枚举类名
    public final static String CLASS_NAME_ENUM = "BDR";
    //枚举类的第一个类型，用于更新所有已绑定的视图
    public final static String ENUM_DEFAULT_PROPERTY = "all";

    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.processingEnvironment = processingEnvironment;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        //添加自定义注解的支持
        annotationTypes.add(Binding.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Elements elementUtils = processingEnvironment.getElementUtils();
        Filer filer = processingEnvironment.getFiler();

        String masterPkg = null;
        Map<String, ClassHolder> bindingMap = new HashMap<>();

        TypeSpec.Builder enumSpecBuilder = TypeSpec.enumBuilder(CLASS_NAME_ENUM)
                .addModifiers(Modifier.PUBLIC).addEnumConstant(ENUM_DEFAULT_PROPERTY);
        Set<String> enumConstants = new HashSet<>();

        for (Element element : roundEnvironment.getElementsAnnotatedWith(Binding.class)) {
            if (element.getKind() == ElementKind.METHOD && element.getModifiers().contains(Modifier.PUBLIC)) {
                ExecutableElement executableElement = (ExecutableElement) element;
                if (executableElement.getParameters().size() == 0) {
                    if (masterPkg == null) {
                        masterPkg = elementUtils.getPackageOf(element).getQualifiedName().toString();
                        //这里以三级包名为模块包名
                        String[] pkgNames = masterPkg.split("\\.");
                        if (pkgNames.length > 3) {
                            masterPkg = String.format("%s.%s.%s", pkgNames[0], pkgNames[1], pkgNames[2]);
                        }
                    }

                    TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();

                    //一个viewModel只生成一个BindingCallbacks
                    String pkgClassName = typeElement.getQualifiedName().toString();
                    ClassHolder classHolder;
                    if (!bindingMap.containsKey(pkgClassName)) {
                        String className = pkgClassName.substring(pkgClassName.lastIndexOf('.') + 1) + "$BindingCallbacks";
                        classHolder = generateClass(className, TypeName.get(typeElement.asType()));
                        bindingMap.put(pkgClassName, classHolder);
                    } else {
                        classHolder = bindingMap.get(pkgClassName);
                    }

                    //当方法名为get和is开头时移除该关键字
                    String functionName = element.getSimpleName().toString();
                    if (functionName.startsWith("get")) {
                        functionName = functionName.substring(3);
                    } else if (functionName.startsWith("is")) {
                        functionName = functionName.substring(2);
                    }

                    //将其后的第一个字母设为小写
                    functionName = functionName.substring(0, 1).toLowerCase() + functionName.substring(1);

                    //将该处理过的方法名作为枚举的一个类别
                    if (!enumConstants.contains(functionName)) {
                        enumSpecBuilder.addEnumConstant(functionName);
                        enumConstants.add(functionName);
                    }
                    //为BindingCallbacks的构造器添加一对枚举类别和该方法的对应关系
                    classHolder.constructorSpecBuilder.addStatement("callbacks.put($T.$L, new $T(){public $T get(){return model.$L();}})", ClassName.bestGuess(masterPkg + "." + CLASS_NAME_ENUM), functionName, ClassName.bestGuess(CLASS_CALLBACK_LISTENER), TypeName.get(executableElement.getReturnType()).box(), executableElement.getSimpleName().toString());
                }
            }
        }

        if (masterPkg != null) {
            for (String key : bindingMap.keySet()) {
                ClassHolder classHolder = bindingMap.get(key);
                classHolder.classSpecBuilder.addMethod(classHolder.constructorSpecBuilder.build());
                //生成BindingCallbacks特定类
                writeClass(key.substring(0, key.lastIndexOf('.')), bindingMap.get(key).classSpecBuilder.build(), filer);
            }
            //生成BDR枚举类
            writeClass(masterPkg, enumSpecBuilder.build(), filer);
        }

        return true;
    }

    /**
     * 将待建类写入文件生成实际的类
     */
    private void writeClass(String packageName, TypeSpec typeSpec, Filer filer) {
        System.out.println("------------------> create class " + packageName + ":" + typeSpec.name);
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .build();
        try {
            javaFile.writeTo(filer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建一个将viewModel为参数的构造器和继承BindingCallbacks的待建类
     */
    private ClassHolder generateClass(String className, TypeName constructorParameter) {
        ClassHolder classHolder = new ClassHolder();

        classHolder.classSpecBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC).superclass(ClassName.bestGuess(CLASS_BINDING_CALLBACKS));

        classHolder.constructorSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC).addParameter(ParameterSpec.builder(constructorParameter, "model").build());

        return classHolder;
    }

    private static class ClassHolder {
        TypeSpec.Builder classSpecBuilder;
        MethodSpec.Builder constructorSpecBuilder;
    }
}
