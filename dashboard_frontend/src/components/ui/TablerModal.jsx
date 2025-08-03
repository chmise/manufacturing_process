import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import TablerButton from './TablerButton';

const TablerModal = ({
  isOpen = false,
  onClose,
  title,
  children,
  footer,
  size = 'md',
  centered = false,
  backdrop = true,
  keyboard = true,
  className = '',
  headerClassName = '',
  bodyClassName = '',
  footerClassName = '',
  ...props
}) => {
  useEffect(() => {
    const handleEscape = (event) => {
      if (keyboard && event.key === 'Escape' && isOpen) {
        onClose?.();
      }
    };

    if (isOpen) {
      document.addEventListener('keydown', handleEscape);
      document.body.style.overflow = 'hidden';
    }

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.overflow = 'unset';
    };
  }, [isOpen, keyboard, onClose]);

  const getSizeClass = () => {
    switch (size) {
      case 'sm':
        return 'modal-sm';
      case 'lg':
        return 'modal-lg';
      case 'xl':
        return 'modal-xl';
      case 'fullscreen':
        return 'modal-fullscreen';
      default:
        return '';
    }
  };

  const handleBackdropClick = (event) => {
    if (backdrop && event.target === event.currentTarget) {
      onClose?.();
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className={`modal fade show ${className}`}
      style={{ display: 'block' }}
      tabIndex="-1"
      onClick={handleBackdropClick}
      {...props}
    >
      <div className={`modal-dialog ${getSizeClass()} ${centered ? 'modal-dialog-centered' : ''}`}>
        <div className="modal-content">
          {title && (
            <div className={`modal-header ${headerClassName}`}>
              <h5 className="modal-title">{title}</h5>
              <button
                type="button"
                className="btn-close"
                onClick={onClose}
                aria-label="Close"
              ></button>
            </div>
          )}
          
          <div className={`modal-body ${bodyClassName}`}>
            {children}
          </div>
          
          {footer && (
            <div className={`modal-footer ${footerClassName}`}>
              {footer}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

// 편의 컴포넌트들
export const ModalFooter = ({ children, className = '' }) => (
  <div className={`d-flex justify-content-end gap-2 ${className}`}>
    {children}
  </div>
);

export const ConfirmModal = ({
  isOpen,
  onClose,
  onConfirm,
  title = '확인',
  message,
  confirmText = '확인',
  cancelText = '취소',
  confirmVariant = 'primary',
  loading = false
}) => {
  return (
    <TablerModal
      isOpen={isOpen}
      onClose={onClose}
      title={title}
      size="sm"
      centered
      footer={
        <ModalFooter>
          <TablerButton
            variant="secondary"
            onClick={onClose}
            disabled={loading}
          >
            {cancelText}
          </TablerButton>
          <TablerButton
            variant={confirmVariant}
            onClick={onConfirm}
            loading={loading}
          >
            {confirmText}
          </TablerButton>
        </ModalFooter>
      }
    >
      {message}
    </TablerModal>
  );
};

TablerModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func,
  title: PropTypes.node,
  children: PropTypes.node.isRequired,
  footer: PropTypes.node,
  size: PropTypes.oneOf(['sm', 'md', 'lg', 'xl', 'fullscreen']),
  centered: PropTypes.bool,
  backdrop: PropTypes.bool,
  keyboard: PropTypes.bool,
  className: PropTypes.string,
  headerClassName: PropTypes.string,
  bodyClassName: PropTypes.string,
  footerClassName: PropTypes.string
};

ModalFooter.propTypes = {
  children: PropTypes.node.isRequired,
  className: PropTypes.string
};

ConfirmModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirm: PropTypes.func.isRequired,
  title: PropTypes.string,
  message: PropTypes.node.isRequired,
  confirmText: PropTypes.string,
  cancelText: PropTypes.string,
  confirmVariant: PropTypes.string,
  loading: PropTypes.bool
};

export default TablerModal;