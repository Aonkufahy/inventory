import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login';
import { AuthGuard } from './guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  
  { 
    path: 'products', 
    loadComponent: () => import('./components/product-list/product-list').then(m => m.ProductListComponent), 
    canActivate: [AuthGuard] 
  },
  
  
  { 
    path: 'admin', 
    loadComponent: () => import('./components/admin/admin').then(m => m.AdminComponent), 
    canActivate: [AuthGuard] 
  },
  
 
  { 
    path: 'orders', 
    loadComponent: () => import('./components/order-history/order-history').then(m => m.OrderHistoryComponent), 
    canActivate: [AuthGuard] 
  },
  
  { path: '**', redirectTo: '/products' }
];