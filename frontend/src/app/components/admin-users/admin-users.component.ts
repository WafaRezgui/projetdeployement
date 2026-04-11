import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserCog, Users, Award, TrendingUp } from 'lucide-angular';
import { UserService, UserDTO } from '../../services/user.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
})
export class AdminUsersComponent implements OnInit {
  readonly UserCogIcon = UserCog;
  readonly UsersIcon = Users;
  readonly AwardIcon = Award;
  readonly TrendingUpIcon = TrendingUp;

  users: UserDTO[] = [];
  loading = false;
  errorMessage = '';

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load users.';
        this.loading = false;
      }
    });
  }
}


