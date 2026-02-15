export interface Order {
  id: number;
  createdAt: string;  // Use createdAt instead of orderDate
  totalAmount: number;
  items: OrderItem[];  // Use items instead of orderItems
}

export interface OrderItem {
  productName: string;  // Direct field
  price: number;  // Direct field
  quantity: number;
}