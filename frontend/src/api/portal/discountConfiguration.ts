import request from "@/utils/request";
import type { DiscountConfigurationForm, DiscountConfigurationVO } from "@/types/api";

const BASE_URL = "/api/v1/discountConfiguration";

const DiscountConfigurationAPI = {
  getAll() {
    return request<any, DiscountConfigurationVO[]>({
      url: BASE_URL,
      method: "get",
    });
  },

  saveAll(data: DiscountConfigurationForm) {
    return request({
      url: BASE_URL,
      method: "post",
      data,
    });
  },
};

export default DiscountConfigurationAPI;
