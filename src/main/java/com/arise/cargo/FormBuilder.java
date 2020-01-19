package com.arise.cargo;

import com.arise.cargo.contexts.HTMLContext;
import com.arise.cargo.model.CGVar;
import com.arise.cargo.model.InputType;
import com.arise.cargo.model.UXField;
import com.arise.core.serializers.parser.Whisker;
import com.arise.core.tools.models.FilterCriteria;
import com.arise.core.tools.StreamUtil;
import com.arise.core.tools.TypeUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.arise.core.tools.CollectionUtil.isEmpty;

public class FormBuilder {
    List<CGVar> variables = new ArrayList<>();
    private String submitBtnTxt = "Submit";
    private String helpFuncName = "$g";

    List<String> jsLines = new ArrayList<>();

    public FormBuilder scan(Object payload){
        return scan(payload.getClass(), payload, variables, null);
    }



    public FormBuilder scan(Class clazz, final Object payload, final List<CGVar> buffer, final CGVar parent) {
        final HTMLContext htmlContext = new HTMLContext();


        TypeUtil.findAllFields(clazz, new FilterCriteria<Field>() {
            @Override
            public boolean isAcceptable(Field field) {
                UXField uxField = field.getAnnotation(UXField.class);
                Object value = null;

                if (uxField != null){


                    if (payload != null) {
                        try {
                            field.setAccessible(true);
                            value = field.get(payload);
                        } catch (Exception e) {
                            value = null;
                        }
                    }

                    CGVar cgVar = new CGVar(uxField, field, value, (parent != null ? parent.getId() : "")).setParentContext(htmlContext);


                    jsLines.add(
                            "$r." + (parent != null ? parent.getName() + "." : "")  + cgVar.getName() + " = "
                            +  (uxField.type().equals(InputType.OBJECT) ? "{};" : helpFuncName + "('" + cgVar.getId() + "');")
                    );



                    if (uxField.type().equals(InputType.OBJECT)){
                        List<CGVar> nextVars =  new ArrayList<>();
                        Class nextClazz;
                        if (value != null){
                            nextClazz = value.getClass();
                        } else {
                            nextClazz = field.getType();
                        }
                        scan(nextClazz, value, nextVars, cgVar);
                        cgVar.setVariables(nextVars);
                    }

                    buffer.add(cgVar);
                    return true;
                }
                return false;
            }
        });
        buffer.sort(new Comparator<CGVar>() {
            @Override
            public int compare(CGVar o1, CGVar o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        return this;
    }


    private Whisker whisker = new Whisker();


    public FormBuilder setButtonText(String text){
        submitBtnTxt = text;
        return this;
    }

    public FormBuilder setHelpFuncName(String helpFuncName) {
        this.helpFuncName = helpFuncName;
        return this;
    }

    public void buildForm(Writer writer, String id){
        StringBuilder jsBuilder = new StringBuilder();
        jsBuilder.append("this.$r = {}; ");

        for (String l: jsLines){
            jsBuilder.append(l);
        }


        jsBuilder.append(id).append("(this.$r);");


        try {
            buildForm(writer, variables);
            InputStream btn = StreamUtil.readResource("/templates/ux_end_btn.whiskey");
            Map<String, String> ob = new HashMap<>();
            ob.put("action", jsBuilder.toString());
            ob.put("btn_text", submitBtnTxt);
            whisker.compile(new InputStreamReader(btn), writer, ob);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildForm(Writer writer, List<CGVar> vars) throws IOException {

        for (CGVar p: vars){
            if (p.getInputType().equals(InputType.OBJECT) && !isEmpty(p.getVariables())){
                InputStream before = StreamUtil.readResource("/templates/ux_field_before_object.whiskey");
                whisker.compile(new InputStreamReader(before), writer, p);

                buildForm(writer, p.getVariables());

                InputStream after = StreamUtil.readResource("/templates/ux_field_after_object.whiskey");
                whisker.compile(new InputStreamReader(after), writer, p);
            }
            else {
                digest(p, writer);
            }
        }
    }

    private void digest(CGVar payload, Writer writer) {
        InputStream inputStream = StreamUtil.readResource("/templates/ux_field.whiskey");
        try {
            whisker.compile(new InputStreamReader(inputStream), writer, payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
