import { getData, postData, putData, deleteData } from "../utils/api";

const API_BASE_URL = "/verifier/admin/v1";

export const fetchFilters = async (page: number, size: number, searchKey: string|null, searchValue: string|null) => {
    const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    });

    if (searchKey && searchValue) {
        params.append("searchKey", searchKey);
        params.append("searchValue", searchValue);
    }

    return getData(API_BASE_URL, `filters?${params.toString()}`);
};

export const postFilter = async (data: any) => {
    return postData(API_BASE_URL, "filters", data);
}

export const getFilter = async (id: number) => {
    return getData(API_BASE_URL, `filters/${id}`);
}

export const putFilter = async (data: any) => {
    return putData(API_BASE_URL, `filters/${data.filterId}`, data);
}

export const deleteFilter = async (id: number) => {    
    return deleteData(API_BASE_URL, `filters/${id}`);
}