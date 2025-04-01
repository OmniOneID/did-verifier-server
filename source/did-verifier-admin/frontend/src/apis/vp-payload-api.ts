import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchServices = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `payloads?${params.toString()}`);
};

export const postService = async (data: any) => {
    return postData(API_BASE_URL, "payloads", data);
}

export const getService = async (id: number) => {
    return getData(API_BASE_URL, `payloads/${id}`);
}

export const putService = async (data: any) => {
    return putData(API_BASE_URL, `payloads/${data.id}`, data);
}

export const deleteService = async (id: number) => {    
    return deleteData(API_BASE_URL, `payloads/${id}`);
}

export const searchServiceList = async (searchValue: string | '') => {    
    return getData(API_BASE_URL, `payloads/popups/${searchValue}`);
}