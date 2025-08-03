import React from 'react';
import PropTypes from 'prop-types';
import StatsCard from '../ui/StatsCard';
import { useCustomizationContext } from '../../hooks/useCustomization';

const FTYStatusNew = ({ fty = 98.5 }) => {
  const { getIndustryTheme } = useCustomizationContext();
  const industry = getIndustryTheme();

  const getFTYGrade = () => {
    if (fty >= 99) return { grade: 'A', color: 'success', trend: 1 };
    if (fty >= 97) return { grade: 'B', color: 'info', trend: 0 };
    if (fty >= 95) return { grade: 'C', color: 'warning', trend: -1 };
    return { grade: 'D', color: 'danger', trend: -1 };
  };

  const ftyGrade = getFTYGrade();

  const qualityIcon = (
    <>
      <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
      <path d="M12 2l3.09 6.26l6.91 1.01l-5 4.87l1.18 6.88l-6.18 -3.25l-6.18 3.25l1.18 -6.88l-5 -4.87l6.91 -1.01z"/>
    </>
  );

  return (
    <StatsCard
      title="초회 통과율 (FTY)"
      value={`${fty.toFixed(1)}%`}
      trend={ftyGrade.trend}
      trendValue={1.2}
      icon={qualityIcon}
      color={ftyGrade.color}
      industry={industry}
    />
  );
};

FTYStatusNew.propTypes = {
  fty: PropTypes.number
};

export default FTYStatusNew;