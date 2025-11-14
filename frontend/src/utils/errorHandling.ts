import axios, { type AxiosError } from 'axios';

type ErrorPayload = {
  message?: string;
};

export const isApiError = (error: unknown): error is AxiosError<ErrorPayload> =>
  axios.isAxiosError(error);

export const getApiErrorMessage = (error: unknown, fallback: string): string => {
  if (isApiError(error)) {
    return error.response?.data?.message || error.message || fallback;
  }

  if (error instanceof Error) {
    return error.message || fallback;
  }

  return fallback;
};

export const getApiErrorStatus = (error: unknown): number | undefined => {
  if (isApiError(error)) {
    return error.response?.status;
  }

  if (typeof error === 'object' && error !== null && 'status' in error) {
    return (error as { status?: number }).status;
  }

  return undefined;
};
