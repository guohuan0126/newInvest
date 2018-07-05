package com.jiuyi.ndr;

import com.jiuyi.ndr.domain.autoinvest.AutoInvest;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Created by zhangyibo on 2017/6/13.
 */
@SpringBootTest
public class ProviderSqlUtil {

    @Test
    public void start(){
        generateUpdateSql(IPlanRepayDetail.class,"ndr_iplan_repay_detail");
//        generateInsertSql(IPlanRepayDetail.class,"ndr_iplan_repay_detail");
    }


    public void generateUpdateSql(Class<?> clazz,String tableName){
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder sqlCode = new StringBuilder("public String updateSql(final ").append(clazz.getSimpleName()).append(" ").append(clazz.getSimpleName().toLowerCase()).append("){\n");
        sqlCode.append("return new SQL(){\n");
        sqlCode.append("{\n");
        sqlCode.append("UPDATE(\"").append(tableName).append("\");").append("\n");
        for(Field field:fields){
            String filedName = field.getName();
            int modifier = field.getModifiers();
            if(modifier==26||modifier==25) continue;
            Type type = field.getType();

            if(type.getTypeName().equals("java.lang.String")){
                sqlCode.append("if(!StringUtils.isEmpty(").append(clazz.getSimpleName().toLowerCase()).append(".")
                        .append("get").append(filedName.substring(0,1).toUpperCase()).append(filedName.substring(1)).append("())");
                sqlCode.append("){\n");
                sqlCode.append("SET(\"").append(fieldNameToColumnName(filedName)).append("=#{").append(filedName).append("}").append("\"").append(");\n");
                sqlCode.append("}");
                sqlCode.append("\n");
            }else{
                sqlCode.append("if(").append(clazz.getSimpleName().toLowerCase()).append(".")
                        .append("get").append(filedName.substring(0,1).toUpperCase()).append(filedName.substring(1)).append("()");
                sqlCode.append("!=null){\n");
                sqlCode.append("SET(\"").append(fieldNameToColumnName(filedName)).append("=#{").append(filedName).append("}").append("\"").append(");\n");
                sqlCode.append("}");
                sqlCode.append("\n");
            }
        }

        sqlCode.append("if(").append(clazz.getSimpleName().toLowerCase()).append(".").append("getUpdateTime()!=null){\n");
        sqlCode.append("SET(\"update_time=#{updateTime}\");\n");
        sqlCode.append("}\n");
        sqlCode.append("WHERE(\"id=#{id}\");\n");
        sqlCode.append("}\n");
        sqlCode.append("}.toString();\n");
        sqlCode.append("}");
        System.out.println(sqlCode.toString());
    }

    private void generateInsertSql(Class<?> clazz,String tableName){
        StringBuilder insertSql = new StringBuilder("INSERT INTO ");
        insertSql.append(tableName).append("(");
        Field[] fields = clazz.getDeclaredFields();
        for(Field field:fields){
            int modifier = field.getModifiers();
            if(modifier==26||modifier==25) continue;
            insertSql.append(fieldNameToColumnName(field.getName())).append(",");
        }
        insertSql.append("create_time) VALUES (");
        for(Field field:fields){
            int modifier = field.getModifiers();
            if(modifier==26||modifier==25) continue;
            insertSql.append("#{").append(field.getName()).append("},");
        }
        insertSql.append("#{createTime})");
        System.out.println(insertSql.toString());
    }

    private String fieldNameToColumnName(String fieldName){
        StringBuilder columnName = new StringBuilder("");
        for(Character c:fieldName.toCharArray()){
            if(Character.isUpperCase(c)){
                columnName.append("_").append((c+"").toLowerCase());
            }else{
                columnName.append(c);
            }
        }

        return columnName.toString();
    }

}
