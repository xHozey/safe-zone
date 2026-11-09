import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';

import { Register } from './register';

describe('Register', () => {
  let component: Register;
  let fixture: ComponentFixture<Register>;
  let httpTesting: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('marks invalid fields and does not send a request', () => {
    component.onSubmit();

    expect(component.registerForm.controls.name.touched).toBe(true);
    expect(component.registerForm.controls.email.touched).toBe(true);
    expect(component.registerForm.controls.password.touched).toBe(true);
    httpTesting.expectNone('https://localhost:8080/api/v1/auth/register');
  });

  it('registers a user then navigates to login', async () => {
    const request = {
      name: 'Seller',
      email: 'seller@example.com',
      password: 'secret1',
      role: 'SELLER' as const,
    };
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    component.registerForm.setValue(request);

    component.onSubmit();

    const registration = httpTesting.expectOne('https://localhost:8080/api/v1/auth/register');
    expect(registration.request.body).toEqual(request);
    registration.flush({ id: 'user-1', name: request.name, email: request.email });

    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith('/login');
  });

  it('shows the server error when registration fails', () => {
    component.registerForm.setValue({
      name: 'Seller',
      email: 'seller@example.com',
      password: 'secret1',
      role: 'SELLER',
    });
    component.onSubmit();

    httpTesting.expectOne('https://localhost:8080/api/v1/auth/register')
      .flush({ message: 'Email is already registered' }, { status: 409, statusText: 'Conflict' });

    expect(component.errorMessage()).toBe('Email is already registered');
    expect(component.isSubmitting()).toBe(false);
  });
});
