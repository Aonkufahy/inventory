import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth';
import { AuthGuard } from '../../../guards/auth-guard';

@Component({
  selector: 'app-login',
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
  standalone:false
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}
login(): void {
  this.authService.login(this.credentials).subscribe({
    next: () => {
      
      if (this.authService.isAdmin()) {
        this.router.navigate(['/admin']);
      } else {
        this.router.navigate(['/products']);
      }
    },
    error: (err) => {
      this.errorMessage = 'Invalid credentials';
    }
  });
}

 
}