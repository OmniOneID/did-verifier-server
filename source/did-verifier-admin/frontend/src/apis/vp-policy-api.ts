import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchPolicies = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `policies?${params.toString()}`);
};

export const postPolicy = async (data: any) => {
    return postData(API_BASE_URL, "policies", data);
}

export const getPolicy = async (id: number) => {
    return getData(API_BASE_URL, `policies/${id}`);
}

export const putPolicy = async (data: any) => {
    return putData(API_BASE_URL, `policies/${data.id}`, data);
}

export const deletePolicy = async (id: number) => {    
    return deleteData(API_BASE_URL, `policies/${id}`);
}
