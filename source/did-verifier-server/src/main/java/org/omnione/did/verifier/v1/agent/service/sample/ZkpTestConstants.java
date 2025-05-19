package org.omnione.did.verifier.v1.agent.service.sample;


import org.omnione.did.zkp.datamodel.credential.AttributeValue;
import org.omnione.did.zkp.datamodel.enums.PredicateType;
import org.omnione.did.zkp.datamodel.proofrequest.AttributeInfo;
import org.omnione.did.zkp.datamodel.proofrequest.PredicateInfo;
import org.omnione.did.zkp.exception.ZkpException;

import java.math.BigInteger;
import java.util.*;

public class ZkpTestConstants {

    public static BigInteger NONCE = new BigInteger("1068995366822249097155600");
    public static String ISSUER_DID = "did:omn:NcYxiDXkpYi6ov5FcYDi1e";
    public static String SCHEMA_ID = "did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0";
    public static String CRED_DEF_ID = "did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0:Tag1";
    public static String REVOC_REG_ID = "did:omn:NcYxiDXkpYi6ov5FcYDi1e:4:did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0:Tag1:CL_ACCUM:Tag1";
    public static String SCHEMA_NAME = "mdl";
    public static String VERSION = "1.0";
    public static final int AGE_CONDITION = 20200103;
    public static final int SALARY_CONDITION = 50000;
    public static final String PROVER_DID = "did:omn:VsKV7grR1BUE29mG2Fm2kX";
    public static final String TAG = "Tag1";
    public static final BigInteger MASTER_SECRET = new BigInteger("70485594559566827707663048943252009495485069724652632864855059894798968061141");


    public static final String SEX = "male";
    public static final String AGE = "18";
    public static final String ADDRESS = "seoul";

    public static List<String> getAttributeList() {

        List<String> attributeList = new LinkedList<String>();
        attributeList.add("zkpsex");
        attributeList.add("zkpbirth");
        attributeList.add("zkpasort");
        attributeList.add("zkpaddr");

        return attributeList;
    }

    public static Map<String, AttributeInfo> getProofRequestAttribute() {

        Map<String, String> restriction = new HashMap<String, String>();
        restriction.put("credDefId", "did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0:Tag1");
        LinkedHashMap<String, AttributeInfo> attributeMap = new LinkedHashMap<String,AttributeInfo>();
        AttributeInfo attributeInfo1 = new AttributeInfo();
        attributeInfo1.setName("MDLNS.zkpsex");
        attributeInfo1.addRestriction(restriction);
        attributeMap.put("attributeReferent1", attributeInfo1);

        AttributeInfo attributeInfo2 = new AttributeInfo();
        attributeInfo2.setName("MDLNS.zkpasort");
        attributeInfo2.addRestriction(restriction);
        attributeMap.put("attributeReferent2", attributeInfo2);

        AttributeInfo attributeInfo3 = new AttributeInfo();
        attributeInfo3.setName("MDLNS.zkpaddr");
        attributeInfo3.addRestriction(restriction);
        attributeMap.put("attributeReferent3", attributeInfo3);

        return attributeMap;
    }

    public static Map<String, PredicateInfo> getProofRequestPredicate() {

        LinkedHashMap<String, PredicateInfo> predicateMap = new LinkedHashMap<String, PredicateInfo>();
        PredicateInfo predicateInfo1 = new PredicateInfo();
        predicateInfo1.setPType(PredicateType.LE);
        predicateInfo1.setName("MDLNS.zkpbirth");
        predicateInfo1.setPValue(ZkpTestConstants.AGE_CONDITION);
        Map<String, String> restriction = new HashMap<String, String>();
        restriction.put("credDefId", "did:omn:NcYxiDXkpYi6ov5FcYDi1e:3:CL:did:omn:NcYxiDXkpYi6ov5FcYDi1e:2:mdl:1.0:Tag1");
        predicateInfo1.addRestriction(restriction);
        predicateMap.put("predicateReferent1", predicateInfo1);

        return predicateMap;
    }

    public static LinkedHashMap<String, AttributeValue> generateAttributeValues() throws ZkpException {

        LinkedHashMap<String, AttributeValue> credValue = new LinkedHashMap<String, AttributeValue>();
        AttributeValue attributeValue_1 = new AttributeValue();
        attributeValue_1.setRaw(SEX);
        AttributeValue attributeValue_2 = new AttributeValue();
        attributeValue_2.setRaw(AGE);
        AttributeValue attributeValue_3 = new AttributeValue();
        attributeValue_3.setRaw(ADDRESS);
        credValue.put("sex", attributeValue_1);
        credValue.put("age", attributeValue_2);
        credValue.put("address", attributeValue_3);

        return credValue;
    }

//    public static LinkedHashMap<String,AttributeValue> genCredentialValue(String schemaStr, String did){
//        String[] value = new String[]{"female","20000101","54321","kimpo"};
//        int cnt = 0;
//        LinkedHashMap<String, AttributeValue> credentialValue = new LinkedHashMap<>();
//        try {
//            CredentialSchema schema = new Gson().fromJson(schemaStr, CredentialSchema.class);
//
//            for (String attrName : schema.getAttrNames()) {
//                AttributeValue attributeValue = new AttributeValue();
//                attributeValue.setRaw(value[cnt]);
//                credentialValue.put(attrName, attributeValue);
//                cnt++;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return credentialValue;
//    }
}
