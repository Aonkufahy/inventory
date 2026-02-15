import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { JwtHelperService } from '@auth0/angular-jwt';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';
  private jwtHelper = new JwtHelperService();
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {
  const token = localStorage.getItem('token');
  if (token && !this.jwtHelper.isTokenExpired(token)) {
    const decoded = this.jwtHelper.decodeToken(token);
 
    const user = { username: decoded.sub, role: decoded.role };
    this.currentUserSubject.next(user);
  }
}

 
  register(user: User) {
  return this.http.post('http://localhost:8080/api/register/user', user);
}


 login(credentials: { username: string; password: string }): Observable<{ token: string }> {
  return this.http.post<{ token: string }>(`${this.baseUrl}/login`, credentials).pipe(
    tap(response => {
      localStorage.setItem('token', response.token);
      const decoded = this.jwtHelper.decodeToken(response.token);
     
      const user = { username: decoded.sub, role: decoded.role };
      this.currentUserSubject.next(user);
    })
  );
}

  logout(): void {
    localStorage.removeItem('token');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  isAdmin(): boolean {
    const user = this.currentUserSubject.value;
    return user?.role === 'ADMIN';
  }

  isAuthenticated(): boolean {
    const token = this.getToken();
    return token != null && !this.jwtHelper.isTokenExpired(token);
  }
}