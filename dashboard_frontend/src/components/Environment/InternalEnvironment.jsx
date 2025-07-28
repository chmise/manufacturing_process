import React from 'react';

const InternalEnvironment = () => {
  return (
    <div className="card" style={{ height: '215px' }}>
      <div className="card-body d-flex flex-column justify-content-center" style={{ height: '215px' }}>
          <h3 className="card-title">내부 환경</h3>
          
          <div className="row mt-3">
            {/* 온도 */}
            <div className="col-4">
              <div className="d-flex align-items-center">
                <div className="me-3" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="url(#tempGradientInternal)" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <defs>
                      <linearGradient id="tempGradientInternal" x1="0%" y1="0%" x2="0%" y2="100%">
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
                  <div className="text-muted">온도</div>
                  <div className="fw-bold h4 mb-0">23°C</div>
                </div>
              </div>
            </div>
            
            {/* 습도 */}
            <div className="col-4">
              <div className="d-flex align-items-center">
                <div className="me-3" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="44" height="44" viewBox="0 0 24 24" fill="#3fa9f5" className="icon icon-tabler icons-tabler-filled icon-tabler-droplet">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M10.708 2.372a2.382 2.382 0 0 0 -.71 .686l-4.892 7.26c-1.981 3.314 -1.22 7.466 1.767 9.882c2.969 2.402 7.286 2.402 10.254 0c2.987 -2.416 3.748 -6.569 1.795 -9.836l-4.919 -7.306c-.722 -1.075 -2.192 -1.376 -3.295 -.686z" />
                  </svg>
                </div>
                <div>
                  <div className="text-muted">습도</div>
                  <div className="fw-bold h4 mb-0">45%</div>
                </div>
              </div>
            </div>
            
            {/* 공기질 */}
            <div className="col-4">
              <div className="d-flex align-items-center">
                <div className="me-3" style={{ width: '48px', height: '48px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="#4CAF50" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <path d="M4 12a8 8 0 1 1 16 0" />
                    <path d="M12 20v-8" />
                    <path d="M8 16l4 -4l4 4" />
                  </svg>
                </div>
                <div>
                  <div className="text-muted">공기질</div>
                  <div className="fw-bold h4 mb-0 text-success">좋음</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
};

export default InternalEnvironment;