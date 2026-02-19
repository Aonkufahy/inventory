import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api';
import { Router } from '@angular/router';
import { Order } from '../../models/order';

@Component({
  selector: 'app-order-history',
  templateUrl: './order-history.html',
  styleUrls: ['./order-history.css'],
  standalone: false
})
export class OrderHistoryComponent implements OnInit {
  orders: Order[] = [];

  constructor(private apiService: ApiService, private router: Router) {}

  ngOnInit(): void {
    this.loadOrderHistory();
  }

  loadOrderHistory(): void {
  console.log('Loading order history, token:', localStorage.getItem('token'));  
  this.apiService.getOrderHistory().subscribe(
    data => {
      console.log('Order history data:', data);  
      this.orders = data;
    },
    error => {
      console.error('Order history error:', error);  
    }
  );
}

  getOrderTotal(order: any): number {
    return order.totalAmount;  
}


  goBack(): void {
    this.router.navigate(['/admin']);
  }
}