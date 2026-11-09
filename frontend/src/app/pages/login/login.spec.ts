import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { Login } from './login';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('marks invalid fields and does not send a request', () => {
    component.onSubmit();

    expect(component.loginForm.controls.email.touched).toBe(true);
    expect(component.loginForm.controls.password.touched).toBe(true);
    httpTesting.expectNone('https://localhost:8080/api/v1/auth/login');
  });

  it('logs in, loads the profile, then navigates home', async () => {
    const request = { email: 'client@example.com', password: 'secret1' };
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component.loginForm.setValue(request);

    component.onSubmit();

    const login = httpTesting.expectOne('https://localhost:8080/api/v1/auth/login');
    expect(login.request.body).toEqual(request);
    login.flush({ token: 'jwt-token' });
    expect(localStorage.getItem('auth_token')).toBe('Bearer jwt-token');
    httpTesting.expectOne('https://localhost:8080/api/v1/users/me').flush({
      id: 'user-1',
      name: 'Client',
      email: request.email,
      avatar: null,
      role: 'CLIENT',
    });

    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith('/');
  });

  it('shows the server error when login fails', () => {
    component.loginForm.setValue({ email: 'client@example.com', password: 'secret1' });
    component.onSubmit();

    httpTesting.expectOne('https://localhost:8080/api/v1/auth/login')
      .flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(component.errorMessage()).toBe('Invalid credentials');
    expect(component.isSubmitting()).toBe(false);
  });
});
