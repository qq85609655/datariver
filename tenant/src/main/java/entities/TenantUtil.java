package entities;

import DataModel.TenantDataModelGenerator;
import org.apache.atlas.typesystem.Referenceable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ShuBoTe 0384
 * @version 1.0.0
 * @company DTDream
 * @date 2015/12/10 13:46
 */
public class TenantUtil{
    private static final String QUALIFIEDNAME_PREFIX = "dr.";
    public static final String QUALIFIED_NAME = "qualifiedName";
    private static final String METASOURCE = "metaSource";
    public static String formatQualifiedName(String... params) {
        String formatedName = doFormat(params);
        if (!formatedName.startsWith(QUALIFIEDNAME_PREFIX)) {
            formatedName = QUALIFIEDNAME_PREFIX + formatedName;
        }
        return formatedName;
    }

    private static String doFormat(String[] params) {
        List<String> nonEmptyParams = new ArrayList<>(params.length);
        for (String p : params) {
            if ((p != null) && (p.length() > 0)) {
                nonEmptyParams.add(p);
            }
        }
        StringBuilder formatString = new StringBuilder();
        for (int i = 0; i < nonEmptyParams.size(); i++) {
            formatString.append("%s").append(".");
        }
        //消除最后多余的"."
        if (formatString.charAt(formatString.length() - 1) == '.') {
            formatString.deleteCharAt(formatString.length() - 1);
        }
        return String.format(formatString.toString(), nonEmptyParams.toArray(new String[0]));
    }

    public static void formateDataElementProperty(Referenceable entity, String qualifiedName){
        entity.set(QUALIFIED_NAME, qualifiedName);
        entity.set(METASOURCE, "DataRiver");
    }

    public static String formateTraitName(String type){
        return TenantDataModelGenerator.TENANT_TRAIT + type;
    }
}
