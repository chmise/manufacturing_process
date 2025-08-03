import { useState, useEffect, useCallback, useRef } from 'react';
import apiService from '../service/apiService';

const useApi = (url, options = {}) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastFetched, setLastFetched] = useState(null);
  
  const abortControllerRef = useRef(null);
  const {
    immediate = true,
    onSuccess,
    onError,
    transform,
    refreshInterval,
    retries = 0,
    retryDelay = 1000,
    dependencies = []
  } = options;

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const executeRequest = useCallback(async (requestUrl = url, requestOptions = {}) => {
    if (!requestUrl) return;

    // Cancel previous request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    abortControllerRef.current = new AbortController();
    
    setLoading(true);
    setError(null);

    let attempts = 0;
    const maxAttempts = retries + 1;

    while (attempts < maxAttempts) {
      try {
        const response = await apiService.get(requestUrl, {
          signal: abortControllerRef.current.signal,
          ...requestOptions
        });

        let responseData = response.data;
        
        // Apply transform if provided
        if (transform) {
          responseData = transform(responseData);
        }

        setData(responseData);
        setLastFetched(new Date());
        setLoading(false);
        
        onSuccess?.(responseData);
        return responseData;

      } catch (err) {
        attempts++;
        
        // If it's an abort error, don't retry
        if (err.name === 'AbortError') {
          setLoading(false);
          return;
        }

        // If we've exhausted retries, set error
        if (attempts >= maxAttempts) {
          const errorMessage = err.response?.data?.message || err.message || 'API 요청 실패';
          setError(errorMessage);
          setLoading(false);
          onError?.(err);
          return;
        }

        // Wait before retry
        if (attempts < maxAttempts) {
          await new Promise(resolve => setTimeout(resolve, retryDelay * attempts));
        }
      }
    }
  }, [url, transform, onSuccess, onError, retries, retryDelay]);

  const refetch = useCallback(() => {
    return executeRequest();
  }, [executeRequest]);

  const mutate = useCallback((newData) => {
    setData(newData);
  }, []);

  // Initial fetch
  useEffect(() => {
    if (immediate && url) {
      executeRequest();
    }

    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [executeRequest, immediate, ...dependencies]);

  // Auto refresh
  useEffect(() => {
    if (!refreshInterval || !url) return;

    const interval = setInterval(() => {
      if (!loading) {
        executeRequest();
      }
    }, refreshInterval);

    return () => clearInterval(interval);
  }, [executeRequest, refreshInterval, loading, url]);

  return {
    data,
    loading,
    error,
    lastFetched,
    refetch,
    mutate,
    clearError,
    execute: executeRequest
  };
};

// POST 요청용 훅
export const useApiPost = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const post = useCallback(async (url, data, options = {}) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiService.post(url, data, options);
      setLoading(false);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'POST 요청 실패';
      setError(errorMessage);
      setLoading(false);
      throw err;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return { post, loading, error, clearError };
};

// PUT 요청용 훅
export const useApiPut = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const put = useCallback(async (url, data, options = {}) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiService.put(url, data, options);
      setLoading(false);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'PUT 요청 실패';
      setError(errorMessage);
      setLoading(false);
      throw err;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return { put, loading, error, clearError };
};

// DELETE 요청용 훅
export const useApiDelete = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const del = useCallback(async (url, options = {}) => {
    setLoading(true);
    setError(null);

    try {
      const response = await apiService.delete(url, options);
      setLoading(false);
      return response.data;
    } catch (err) {
      const errorMessage = err.response?.data?.message || err.message || 'DELETE 요청 실패';
      setError(errorMessage);
      setLoading(false);
      throw err;
    }
  }, []);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return { delete: del, loading, error, clearError };
};

export default useApi;