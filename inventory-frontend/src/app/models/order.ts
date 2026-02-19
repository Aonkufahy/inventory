export interface Order {
  id: number;
  createdAt: string;  
  totalAmount: number;
  items: OrderItem[];  
}

export interface OrderItem {
  productName: string;  
  price: number;  
  quantity: number;
}