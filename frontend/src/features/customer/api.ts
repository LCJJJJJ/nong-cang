import { request } from '../../api/http'

import type {
  CustomerDetail,
  CustomerFormPayload,
  CustomerListItem,
  CustomerListQuery,
  CustomerOption,
} from './types'

export function getCustomerList(query?: CustomerListQuery) {
  return request<CustomerListItem[]>({
    method: 'GET',
    url: '/customer/list',
    params: query,
  })
}

export function getCustomerOptions() {
  return request<CustomerOption[]>({
    method: 'GET',
    url: '/customer/options',
  })
}

export function getCustomerDetail(customerId: string) {
  return request<CustomerDetail>({
    method: 'GET',
    url: `/customer/${customerId}`,
  })
}

export function createCustomer(payload: CustomerFormPayload) {
  return request<CustomerDetail>({
    method: 'POST',
    url: '/customer',
    data: payload,
  })
}

export function updateCustomer(customerId: string, payload: CustomerFormPayload) {
  return request<CustomerDetail>({
    method: 'PUT',
    url: `/customer/${customerId}`,
    data: payload,
  })
}

export function updateCustomerStatus(customerId: string, status: number) {
  return request<void>({
    method: 'PATCH',
    url: `/customer/${customerId}/status`,
    data: { status },
  })
}

export function deleteCustomer(customerId: string) {
  return request<void>({
    method: 'DELETE',
    url: `/customer/${customerId}`,
  })
}
