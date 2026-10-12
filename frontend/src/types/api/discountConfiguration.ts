export interface DiscountRule {
  minPeople: number;
  maxPeople: number;
  discount: number;
}

export interface DiscountConfigurationForm {
  list: DiscountRule[];
}

export interface DiscountConfigurationVO {
  id: number;
  minPeople: number;
  maxPeople: number;
  discount: number;
}
