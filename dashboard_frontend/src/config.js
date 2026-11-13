// 백엔드 주소 설정
//
// VITE_API_URL을 비워두면(기본값) 지금 페이지와 같은 주소의 /api, /ws를 쓴다.
// - 개발 서버(npm run dev): vite.config.js 프록시가 http://localhost:8080으로 넘겨준다
// - 배포(nginx): nginx.conf가 백엔드 컨테이너로 넘겨준다
// 프론트엔드와 백엔드를 서로 다른 주소에 띄울 때만 VITE_API_URL에 백엔드 주소를 넣는다.
const trimTrailingSlash = (url) => url.replace(/\/+$/, '');

export const API_ORIGIN = trimTrailingSlash(import.meta.env.VITE_API_URL ?? '');

export const API_BASE_URL = `${API_ORIGIN}/api`;

// SockJS는 절대 주소가 필요해서 비어 있으면 현재 페이지 주소를 쓴다
export const getWebSocketUrl = () => `${API_ORIGIN || window.location.origin}/ws`;
