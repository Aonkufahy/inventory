import { Component } from '@angular/core'; 
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth';
import { User } from '../../../models/user';

@Component({
  selector: 'app-register',
  templateUrl: './register.html',
  styleUrls: ['./register.css'],
  standalone: false
})
export class RegisterComponent {
  user: User = { username: '', password: '', email: '', role: 'USER' }; 
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  register(): void {
    if (!this.user.email) {
      this.errorMessage = 'Email is required';
      return;
    }

    this.authService.register(this.user).subscribe({
      next: () => this.router.navigate(['/login']),
      error: (err) => {
        console.error(err);
        this.errorMessage = 'Registration failed. Make sure all fields are valid.';
      }
    });
  }
}
