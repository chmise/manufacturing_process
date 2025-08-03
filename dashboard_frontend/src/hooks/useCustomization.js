import { useState, useEffect, useCallback, useContext, createContext } from 'react';
import useApi, { useApiPost, useApiPut } from './useApi';

// 커스터마이징 컨텍스트
const CustomizationContext = createContext();

export const useCustomizationContext = () => {
  const context = useContext(CustomizationContext);
  if (!context) {
    throw new Error('useCustomizationContext must be used within a CustomizationProvider');
  }
  return context;
};

// 커스터마이징 프로바이더
export const CustomizationProvider = ({ children, companyId }) => {
  const [customization, setCustomization] = useState({
    profile: null,
    configurations: {},
    theme: 'automotive',
    setupCompleted: false,
    loading: true
  });

  // 회사 프로필 조회
  const { data: setupStatus, loading: setupLoading } = useApi(
    companyId ? `/api/customization/setup-status/${companyId}` : null,
    {
      immediate: true,
      onSuccess: (data) => {
        setCustomization(prev => ({
          ...prev,
          setupCompleted: data.setupCompleted,
          profile: data.setupCompleted ? data : null,
          loading: false
        }));
      }
    }
  );

  // 커스터마이징 설정 조회
  const { data: configurations, loading: configLoading } = useApi(
    companyId && customization.setupCompleted 
      ? `/api/customization/configurations/${companyId}` 
      : null,
    {
      immediate: customization.setupCompleted,
      transform: (data) => {
        // 배열을 객체로 변환
        const configMap = {};
        data.forEach(config => {
          if (!configMap[config.configCategory]) {
            configMap[config.configCategory] = {};
          }
          configMap[config.configCategory][config.configKey] = config.configValue;
        });
        return configMap;
      },
      onSuccess: (data) => {
        setCustomization(prev => ({
          ...prev,
          configurations: data,
          loading: false
        }));
      }
    }
  );

  const getConfiguration = useCallback((category, key, defaultValue = null) => {
    return customization.configurations[category]?.[key] || defaultValue;
  }, [customization.configurations]);

  const getKPIList = useCallback(() => {
    const kpiString = getConfiguration('kpi', 'selected_kpis', '');
    return kpiString ? kpiString.split(',') : [];
  }, [getConfiguration]);

  const getDashboardSettings = useCallback(() => {
    return customization.configurations.dashboard || {};
  }, [customization.configurations]);

  const getAlertSettings = useCallback(() => {
    return customization.configurations.alert || {};
  }, [customization.configurations]);

  const getIndustryTheme = useCallback(() => {
    return customization.profile?.industryType?.toLowerCase() || 'automotive';
  }, [customization.profile]);

  const getCompanyInfo = useCallback(() => {
    return {
      industryType: customization.profile?.industryType,
      companySize: customization.profile?.companySize,
      productionType: customization.profile?.productionType,
      automationLevel: customization.profile?.automationLevel
    };
  }, [customization.profile]);

  const value = {
    ...customization,
    getConfiguration,
    getKPIList,
    getDashboardSettings,
    getAlertSettings,
    getIndustryTheme,
    getCompanyInfo,
    refresh: () => {
      // 데이터 새로고침 로직
      window.location.reload();
    }
  };

  return (
    <CustomizationContext.Provider value={value}>
      {children}
    </CustomizationContext.Provider>
  );
};

// 커스터마이징 설정 훅
const useCustomization = (companyId) => {
  const [profile, setProfile] = useState(null);
  const [configurations, setConfigurations] = useState({});
  const [setupCompleted, setSetupCompleted] = useState(false);
  const [loading, setLoading] = useState(true);

  const { post: postStep1 } = useApiPost();
  const { post: postStep2 } = useApiPost();
  const { post: postStep3 } = useApiPost();
  const { post: postComplete } = useApiPost();

  // 설정 상태 조회
  const { 
    data: setupStatus, 
    loading: statusLoading, 
    refetch: refetchStatus 
  } = useApi(
    companyId ? `/api/customization/setup-status/${companyId}` : null,
    {
      immediate: !!companyId,
      onSuccess: (data) => {
        setSetupCompleted(data.setupCompleted);
        if (data.setupCompleted) {
          setProfile(data);
        }
      }
    }
  );

  // 커스터마이징 설정 조회
  const { 
    data: configData, 
    loading: configLoading, 
    refetch: refetchConfigurations 
  } = useApi(
    companyId && setupCompleted 
      ? `/api/customization/configurations/${companyId}` 
      : null,
    {
      immediate: setupCompleted,
      transform: (data) => {
        const configMap = {};
        data.forEach(config => {
          if (!configMap[config.configCategory]) {
            configMap[config.configCategory] = {};
          }
          configMap[config.configCategory][config.configKey] = config.configValue;
        });
        return configMap;
      },
      onSuccess: (data) => {
        setConfigurations(data);
      }
    }
  );

  // 업종 목록 조회
  const { data: industries } = useApi('/api/customization/industries', {
    immediate: true
  });

  // 1단계 설정
  const submitStep1 = useCallback(async (data) => {
    try {
      const response = await postStep1(`/api/customization/setup/step1/${companyId}`, data);
      await refetchStatus();
      return response;
    } catch (error) {
      throw error;
    }
  }, [companyId, postStep1, refetchStatus]);

  // 2단계 설정
  const submitStep2 = useCallback(async (data) => {
    try {
      const response = await postStep2(`/api/customization/setup/step2/${companyId}`, data);
      await refetchStatus();
      return response;
    } catch (error) {
      throw error;
    }
  }, [companyId, postStep2, refetchStatus]);

  // 3단계 설정
  const submitStep3 = useCallback(async (data) => {
    try {
      const response = await postStep3(`/api/customization/setup/step3/${companyId}`, data);
      await refetchStatus();
      return response;
    } catch (error) {
      throw error;
    }
  }, [companyId, postStep3, refetchStatus]);

  // 설정 완료
  const completeSetup = useCallback(async () => {
    try {
      const response = await postComplete(`/api/customization/setup/complete/${companyId}`, {
        confirmSetup: true
      });
      await refetchStatus();
      await refetchConfigurations();
      return response;
    } catch (error) {
      throw error;
    }
  }, [companyId, postComplete, refetchStatus, refetchConfigurations]);

  // 편의 메서드들
  const getConfiguration = useCallback((category, key, defaultValue = null) => {
    return configurations[category]?.[key] || defaultValue;
  }, [configurations]);

  const getSelectedKPIs = useCallback(() => {
    const kpiString = getConfiguration('kpi', 'selected_kpis', '');
    return kpiString ? kpiString.split(',') : [];
  }, [getConfiguration]);

  const getIndustryTheme = useCallback(() => {
    return profile?.industryType?.toLowerCase() || 'automotive';
  }, [profile]);

  const isKPISelected = useCallback((kpiName) => {
    const selectedKPIs = getSelectedKPIs();
    return selectedKPIs.includes(kpiName);
  }, [getSelectedKPIs]);

  useEffect(() => {
    setLoading(statusLoading || configLoading);
  }, [statusLoading, configLoading]);

  return {
    // 상태
    profile,
    configurations,
    setupCompleted,
    loading,
    industries,

    // 설정 메서드
    submitStep1,
    submitStep2,
    submitStep3,
    completeSetup,

    // 편의 메서드
    getConfiguration,
    getSelectedKPIs,
    getIndustryTheme,
    isKPISelected,

    // 새로고침
    refetch: () => {
      refetchStatus();
      if (setupCompleted) {
        refetchConfigurations();
      }
    }
  };
};

export default useCustomization;