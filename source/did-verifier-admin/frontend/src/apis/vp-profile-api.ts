import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchProfiles = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `profiles?${params.toString()}`);
};

export const postProfile = async (data: any) => {
    return postData(API_BASE_URL, "profiles", data);
}

export const getProfile = async (id: number) => {
    return getData(API_BASE_URL, `profiles/${id}`);
}

export const putProfile = async (data: any) => {
    return putData(API_BASE_URL, `profiles/${data.id}`, data);
}

export const deleteProfile = async (id: number) => {    
    return deleteData(API_BASE_URL, `profiles/${id}`);
}