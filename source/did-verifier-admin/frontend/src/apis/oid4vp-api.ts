import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

// OID4VP Config
export const getOid4vpConfig = async () => {
    return getData(API_BASE_URL, "oid4vp/config");
};

export const putOid4vpConfig = async (data: any) => {
    return putData(API_BASE_URL, "oid4vp/config", data);
};

// DCQL Scope Mapping
export const fetchScopeMappings = async (page: number, size: number) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });
    return getData(API_BASE_URL, `oid4vp/scope-mappings?${params.toString()}`);
};

export const getScopeMapping = async (id: number) => {
    return getData(API_BASE_URL, `oid4vp/scope-mappings/${id}`);
};

export const postScopeMapping = async (data: any) => {
    return postData(API_BASE_URL, "oid4vp/scope-mappings", data);
};

export const putScopeMapping = async (id: number, data: any) => {
    return putData(API_BASE_URL, `oid4vp/scope-mappings/${id}`, data);
};

export const deleteScopeMapping = async (id: number) => {
    return deleteData(API_BASE_URL, `oid4vp/scope-mappings/${id}`);
};

export const searchScopeMappingList = async (searchValue: string) => {
    return getData(API_BASE_URL, `oid4vp/scope-mappings/popups/${searchValue}`);
};

// OID4VP Policy (uses existing policy API with protocolType=OID4VP)
export const fetchOid4vpPolicies = async (page: number, size: number, searchKey: string | null, searchValue: string | null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        protocolType: "OID4VP",
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `policies?${params.toString()}`);
};

export const postOid4vpPolicy = async (data: any) => {
    return postData(API_BASE_URL, "policies", { ...data, protocolType: "OID4VP" });
};

export const getOid4vpPolicy = async (id: number) => {
    return getData(API_BASE_URL, `policies/${id}`);
};

export const putOid4vpPolicy = async (data: any) => {
    return putData(API_BASE_URL, `policies/${data.id}`, data);
};

export const deleteOid4vpPolicy = async (id: number) => {
    return deleteData(API_BASE_URL, `policies/${id}`);
};

