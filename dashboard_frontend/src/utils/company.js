// 회사 관련 공통 함수

// 회사 경로가 아닌 첫 번째 주소 조각
const NON_COMPANY_PATHS = ['login', 'register', 'company-register'];

// 현재 주소(/{회사명}/...)에서 회사명을 꺼낸다.
// 주소창 값은 인코딩돼 있어서(예: 한글 회사명) 원래 이름으로 되돌려서 돌려준다.
export const getCompanyNameFromPath = (pathname = window.location.pathname) => {
  const first = pathname.split('/').filter(Boolean)[0];
  if (!first || NON_COMPANY_PATHS.includes(first)) {
    return null;
  }
  try {
    return decodeURIComponent(first);
  } catch {
    return first;
  }
};
