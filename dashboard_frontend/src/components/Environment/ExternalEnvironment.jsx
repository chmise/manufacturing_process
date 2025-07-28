import { useState, useEffect } from 'react';

const ExternalEnvironment = () => {
  const [weatherData, setWeatherData] = useState({
    temperature: 25,
    humidity: 55,
    description: '맑음',
    icon: 'sun'
  });
  const [loading, setLoading] = useState(false);

  const fetchWeatherData = async () => {
    try {
      setLoading(true);
      const API_KEY = import.meta.env.VITE_OPENWEATHER_API_KEY;
      if (!API_KEY) {
        console.warn('OpenWeatherMap API key not found in environment variables');
        return;
      }

      // 아산시의 위도, 경도
      const lat = 36.7898;
      const lon = 127.2014;
      
      const response = await fetch(
        `https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${API_KEY}&units=metric&lang=kr`
      );
      
      if (!response.ok) {
        throw new Error('날씨 데이터를 가져오는데 실패했습니다');
      }
      
      const data = await response.json();
      console.log('OpenWeatherMap API 응답:', data);
      
      const newWeatherData = {
        temperature: Math.round(data.main.temp),
        humidity: data.main.humidity,
        description: data.weather[0].description,
        icon: getWeatherIcon(data.weather[0].main)
      };
      
      console.log('업데이트될 날씨 데이터:', newWeatherData);
      setWeatherData(newWeatherData);
    } catch (error) {
      console.error('날씨 데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  const getWeatherIcon = (weatherMain) => {
    switch (weatherMain) {
      case 'Clear':
        return 'sun';
      case 'Clouds':
        return 'cloud';
      case 'Rain':
      case 'Drizzle':
        return 'rain';
      case 'Snow':
        return 'snow';
      case 'Thunderstorm':
        return 'storm';
      default:
        return 'sun';
    }
  };

  const getWeatherSvg = (iconType) => {
    switch (iconType) {
      case 'sun':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#FF6F00" className="icon icon-tabler icons-tabler-filled icon-tabler-sun">
            <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
            <path d="M12 19a1 1 0 0 1 .993 .883l.007 .117v1a1 1 0 0 1 -1.993 .117l-.007 -.117v-1a1 1 0 0 1 1 -1z" />
            <path d="M18.313 16.91l.094 .083l.7 .7a1 1 0 0 1 -1.32 1.497l-.094 -.083l-.7 -.7a1 1 0 0 1 1.218 -1.567l.102 .07z" />
            <path d="M7.007 16.993a1 1 0 0 1 .083 1.32l-.083 .094l-.7 .7a1 1 0 0 1 -1.497 -1.32l.083 -.094l.7 -.7a1 1 0 0 1 1.414 0z" />
            <path d="M4 11a1 1 0 0 1 .117 1.993l-.117 .007h-1a1 1 0 0 1 -.117 -1.993l.117 -.007h1z" />
            <path d="M21 11a1 1 0 0 1 .117 1.993l-.117 .007h-1a1 1 0 0 1 -.117 -1.993l.117 -.007h1z" />
            <path d="M6.213 4.81l.094 .083l.7 .7a1 1 0 0 1 -1.32 1.497l-.094 -.083l-.7 -.7a1 1 0 0 1 1.217 -1.567l.102 .07z" />
            <path d="M19.107 4.893a1 1 0 0 1 .083 1.32l-.083 .094l-.7 .7a1 1 0 0 1 -1.497 -1.32l.083 -.094l.7 -.7a1 1 0 0 1 1.414 0z" />
            <path d="M12 2a1 1 0 0 1 .993 .883l.007 .117v1a1 1 0 0 1 -1.993 .117l-.007 -.117v-1a1 1 0 0 1 1 -1z" />
            <path d="M12 7a5 5 0 1 1 -4.995 5.217l-.005 -.217l.005 -.217a5 5 0 0 1 4.995 -4.783z" />
          </svg>
        );
      case 'cloud':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#9E9E9E">
            <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
            <path d="M22.42 9.42a8.5 8.5 0 0 0 -15.92 .42a6 6 0 0 0 .38 11.92h14.12a4.5 4.5 0 0 0 1.42 -8.34z" />
          </svg>
        );
      case 'rain':
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#2196F3">
            <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
            <path d="M22.42 9.42a8.5 8.5 0 0 0 -15.92 .42a6 6 0 0 0 .38 11.92h14.12a4.5 4.5 0 0 0 1.42 -8.34z" />
          </svg>
        );
      default:
        return (
          <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#FF6F00" className="icon icon-tabler icons-tabler-filled icon-tabler-sun">
            <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
            <path d="M12 19a1 1 0 0 1 .993 .883l.007 .117v1a1 1 0 0 1 -1.993 .117l-.007 -.117v-1a1 1 0 0 1 1 -1z" />
            <path d="M18.313 16.91l.094 .083l.7 .7a1 1 0 0 1 -1.32 1.497l-.094 -.083l-.7 -.7a1 1 0 0 1 1.218 -1.567l.102 .07z" />
            <path d="M7.007 16.993a1 1 0 0 1 .083 1.32l-.083 .094l-.7 .7a1 1 0 0 1 -1.497 -1.32l.083 -.094l.7 -.7a1 1 0 0 1 1.414 0z" />
            <path d="M4 11a1 1 0 0 1 .117 1.993l-.117 .007h-1a1 1 0 0 1 -.117 -1.993l.117 -.007h1z" />
            <path d="M21 11a1 1 0 0 1 .117 1.993l-.117 .007h-1a1 1 0 0 1 -.117 -1.993l.117 -.007h1z" />
            <path d="M6.213 4.81l.094 .083l.7 .7a1 1 0 0 1 -1.32 1.497l-.094 -.083l-.7 -.7a1 1 0 0 1 1.217 -1.567l.102 .07z" />
            <path d="M19.107 4.893a1 1 0 0 1 .083 1.32l-.083 .094l-.7 .7a1 1 0 0 1 -1.497 -1.32l.083 -.094l.7 -.7a1 1 0 0 1 1.414 0z" />
            <path d="M12 2a1 1 0 0 1 .993 .883l.007 .117v1a1 1 0 0 1 -1.993 .117l-.007 -.117v-1a1 1 0 0 1 1 -1z" />
            <path d="M12 7a5 5 0 1 1 -4.995 5.217l-.005 -.217l.005 -.217a5 5 0 0 1 4.995 -4.783z" />
          </svg>
        );
    }
  };

  useEffect(() => {
    fetchWeatherData();
    const interval = setInterval(fetchWeatherData, 300000); // 5분마다 업데이트
    
    return () => clearInterval(interval);
  }, []);

  return (
    <div className="card" style={{ height: '180px' }}>
      <div className="card-body d-flex flex-column justify-content-center" style={{ height: '180px' }}>
          <h3 className="card-title">외부 환경</h3>
          
          <div className="row mt-3">
            {/* 온도 */}
            <div className="col-4">
              <div className="d-flex flex-column align-items-center text-center">
                <div className="mb-2" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="url(#tempGradientExternal)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <defs>
                      <linearGradient id="tempGradientExternal" x1="0%" y1="0%" x2="0%" y2="100%">
                        <stop offset="0%" stopColor="#FF6B6B"/>
                        <stop offset="50%" stopColor="#FFA94D"/>
                        <stop offset="100%" stopColor="#4DABF7"/>
                      </linearGradient>
                    </defs>
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
                    <path d="M10 13.5a4 4 0 1 0 4 0v-8.5a2 2 0 0 0 -4 0v8.5" />
                    <path d="M10 9l4 0" />
                  </svg>
                </div>
                <div>
                  <div className="text-muted small">온도</div>
                  <div className="fw-bold h5 mb-0">{weatherData.temperature}°C</div>
                </div>
              </div>
            </div>
            
            {/* 습도 */}
            <div className="col-4">
              <div className="d-flex flex-column align-items-center text-center">
                <div className="mb-2" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#3fa9f5" className="icon icon-tabler icons-tabler-filled icon-tabler-droplet">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M10.708 2.372a2.382 2.382 0 0 0 -.71 .686l-4.892 7.26c-1.981 3.314 -1.22 7.466 1.767 9.882c2.969 2.402 7.286 2.402 10.254 0c2.987 -2.416 3.748 -6.569 1.795 -9.836l-4.919 -7.306c-.722 -1.075 -2.192 -1.376 -3.295 -.686z" />
                  </svg>
                </div>
                <div>
                  <div className="text-muted small">습도</div>
                  <div className="fw-bold h5 mb-0">{weatherData.humidity}%</div>
                </div>
              </div>
            </div>
            
            {/* 날씨 */}
            <div className="col-4">
              <div className="d-flex flex-column align-items-center text-center">
                <div className="mb-2" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  {getWeatherSvg(weatherData.icon)}
                </div>
                <div>
                  <div className="text-muted small">날씨</div>
                  <div className="fw-bold" style={{ fontSize: '0.7rem', lineHeight: '1.1' }}>{weatherData.description}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default ExternalEnvironment;