import React from 'react';
import PropTypes from 'prop-types';
import StatsCard from '../ui/StatsCard';
import { useCustomizationContext } from '../../hooks/useCustomization';

const OTDStatusNew = ({ otd = 95.2 }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const getOTDGrade = () => {
    if (otd >= 98) return { grade: 'A', color: 'success', trend: 1 };
    if (otd >= 95) return { grade: 'B', color: 'info', trend: 0 };
    if (otd >= 90) return { grade: 'C', color: 'warning', trend: -1 };
    return { grade: 'D', color: 'danger', trend: -1 };
  };

  const otdGrade = getOTDGrade();

  const deliveryIcon = (
    <>
      <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
      <circle cx="7" cy="17" r="2"/>
      <circle cx="17" cy="17" r="2"/>
      <path d="M5 17h-2v-6l2-5h9l4 5h1a2 2 0 0 1 2 2v4h-2m-4 0h-6m-6 -17v16m-2 0h4"/>
    </>
  );

  return (
    <StatsCard
      title="정시 납기율 (OTD)"
      value={`${otd.toFixed(1)}%`}
      trend={otdGrade.trend}
      trendValue={-0.8}
      icon={deliveryIcon}
      color={otdGrade.color}
      industry={industry}
    />
  );
};

OTDStatusNew.propTypes = {
  otd: PropTypes.number
};

export default OTDStatusNew;