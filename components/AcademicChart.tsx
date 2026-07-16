import React from 'react';
import { StyleSheet, View, Text, ActivityIndicator } from 'react-native';
import { WebView } from 'react-native-webview';
import { Colors, Radius, Spacing, Shadows } from '@/constants/theme';

interface ExamResult {
  exam_id: string;
  marks: number | null;
  grade: string | null;
  exam: {
    name: string;
    subject: string;
    exam_date: string;
    max_marks: number;
  };
}

interface AcademicChartProps {
  results: ExamResult[];
}

export function AcademicChart({ results }: AcademicChartProps) {
  // Process data for WebView
  const chartData = results
    .filter(r => r.marks !== null && r.exam?.max_marks)
    .map(r => ({
      exam_name: r.exam.name,
      subject: r.exam.subject,
      date: r.exam.exam_date,
      score_pct: Math.round((r.marks! / r.exam.max_marks) * 100),
      marks: r.marks,
      max_marks: r.exam.max_marks,
      grade: r.grade || '',
    }));

  if (chartData.length === 0) {
    return (
      <View style={styles.emptyContainer}>
        <Text style={styles.emptyText}>No graded exam results found for trends.</Text>
      </View>
    );
  }

  // Create full HTML source loading React & Recharts via CDN
  const htmlContent = `
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <style>
        * {
          box-sizing: border-box;
        }
        body {
          margin: 0;
          padding: 0;
          background-color: #FFFFFF;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
          user-select: none;
          overflow: hidden;
        }
        #app {
          width: 100%;
          height: 100vh;
          display: flex;
          flex-direction: column;
          padding: 8px 4px;
        }
        .tabs-container {
          display: flex;
          overflow-x: auto;
          gap: 8px;
          padding: 4px 12px 12px 12px;
          scrollbar-width: none;
          -webkit-overflow-scrolling: touch;
        }
        .tabs-container::-webkit-scrollbar {
          display: none;
        }
        .tab {
          padding: 6px 14px;
          border-radius: 20px;
          font-size: 12px;
          font-weight: bold;
          white-space: nowrap;
          border: 1px solid #E2E7F2;
          background-color: #FFFFFF;
          color: #4A5878;
          cursor: pointer;
          transition: all 0.2s ease;
        }
        .tab.active {
          background-color: #0F2A5C;
          color: #FFFFFF;
          border-color: #0F2A5C;
        }
        #chart-container {
          flex: 1;
          width: 100%;
          min-height: 200px;
        }
        .loader {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          font-size: 14px;
          color: #8892AB;
          font-weight: 500;
        }
      </style>
      
      <!-- Load React, ReactDOM, PropTypes -->
      <script src="https://unpkg.com/react@18.2.0/umd/react.production.min.js" crossorigin></script>
      <script src="https://unpkg.com/react-dom@18.2.0/umd/react-dom.production.min.js" crossorigin></script>
      <script src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js" crossorigin></script>
      
      <!-- Load Recharts -->
      <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.js" crossorigin></script>
    </head>
    <body>
      <div id="app">
        <div class="loader" id="spinner">Loading charts...</div>
      </div>

      <script>
        const rawData = ${JSON.stringify(chartData)};

        // Hide spinner once scripts are loaded
        document.getElementById('spinner').style.display = 'none';

        const { useState, useMemo } = React;
        const { ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend } = Recharts;

        // Sort data chronologically
        rawData.sort((a, b) => new Date(a.date) - new Date(b.date));

        // Format Date to short string, e.g. "Jun 15"
        const formatShortDate = (dateStr) => {
          try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
          } catch (e) {
            return dateStr;
          }
        };

        const uniqueSubjects = [...new Set(rawData.map(d => d.subject))];
        const colors = ['#0F2A5C', '#FF7A2E', '#1FA971', '#2A6FDB', '#E8A317', '#E0414C'];
        const subjectColors = {};
        uniqueSubjects.forEach((sub, i) => {
          subjectColors[sub] = colors[i % colors.length];
        });

        function ChartApp() {
          const [selectedSubject, setSelectedSubject] = useState('All');

          const displayData = useMemo(() => {
            if (selectedSubject !== 'All') {
              return rawData
                .filter(d => d.subject === selectedSubject)
                .map(d => ({
                  ...d,
                  formattedDate: formatShortDate(d.date),
                  score_pct: d.score_pct
                }));
            } else {
              // Group by date to show multiple subject lines on same date
              const groups = {};
              rawData.forEach(d => {
                const dateKey = d.date;
                if (!groups[dateKey]) {
                  groups[dateKey] = {
                    date: dateKey,
                    formattedDate: formatShortDate(dateKey),
                  };
                }
                groups[dateKey][d.subject] = d.score_pct;
              });
              return Object.values(groups).sort((a, b) => new Date(a.date) - new Date(b.date));
            }
          }, [selectedSubject]);

          const CustomTooltip = ({ active, payload, label }) => {
            if (active && payload && payload.length) {
              const isSingle = selectedSubject !== 'All';
              const displayLabel = payload[0].payload.exam_name || formatShortDate(label);
              
              return React.createElement(
                'div',
                {
                  style: {
                    backgroundColor: '#FFFFFF',
                    border: '1px solid #E2E7F2',
                    borderRadius: '8px',
                    padding: '8px 12px',
                    boxShadow: '0 4px 12px rgba(15, 42, 92, 0.08)'
                  }
                },
                React.createElement('p', { style: { margin: 0, fontSize: '11px', color: '#8892AB', fontWeight: 'bold', marginBottom: '4px' } }, displayLabel),
                payload.map((item, index) => {
                  let name = item.name;
                  let val = item.value;
                  let detailStr = '';
                  const itemColor = item.color || '#0F2A5C';

                  if (isSingle) {
                    name = selectedSubject;
                    const rawItem = rawData.find(d => d.date === label && d.subject === selectedSubject);
                    detailStr = rawItem ? ' (' + rawItem.marks + '/' + rawItem.max_marks + ') - ' + rawItem.grade : '';
                  } else {
                    const rawItem = rawData.find(d => d.date === label && d.subject === item.name);
                    detailStr = rawItem ? ' (' + rawItem.marks + '/' + rawItem.max_marks + ') - ' + rawItem.grade : '';
                  }

                  return React.createElement(
                    'div',
                    { key: index, style: { display: 'flex', alignItems: 'center', gap: '6px', margin: '4px 0 0 0' } },
                    React.createElement('span', { style: { width: '8px', height: '8px', borderRadius: '50%', backgroundColor: itemColor, display: 'inline-block' } }),
                    React.createElement('p', { style: { margin: 0, fontSize: '12px', color: '#0E1A33', fontWeight: '600' } }, 
                      name + ': ' + val + '%' + detailStr
                    )
                  );
                })
              );
            }
            return null;
          };

          return React.createElement(
            'div',
            { style: { display: 'flex', flexDirection: 'column', height: '100%' } },
            
            // Tabs Bar
            React.createElement(
              'div',
              { className: 'tabs-container' },
              React.createElement(
                'button',
                {
                  className: 'tab' + (selectedSubject === 'All' ? ' active' : ''),
                  onClick: () => setSelectedSubject('All')
                },
                'All Subjects'
              ),
              uniqueSubjects.map(sub => 
                React.createElement(
                  'button',
                  {
                    key: sub,
                    className: 'tab' + (selectedSubject === sub ? ' active' : ''),
                    onClick: () => setSelectedSubject(sub)
                  },
                  sub
                )
              )
            ),

            // Recharts Container
            React.createElement(
              'div',
              { id: 'chart-container' },
              React.createElement(
                ResponsiveContainer,
                { width: '100%', height: '100%' },
                React.createElement(
                  LineChart,
                  { data: displayData, margin: { top: 12, right: 16, left: -20, bottom: 4 } },
                  React.createElement(CartesianGrid, { strokeDasharray: '3 3', stroke: '#F0F2F7', vertical: false }),
                  React.createElement(XAxis, { 
                    dataKey: selectedSubject === 'All' ? 'date' : 'date', 
                    tickFormatter: formatShortDate,
                    stroke: '#8892AB', 
                    fontSize: 10,
                    fontWeight: 500,
                    tickLine: false,
                    axisLine: false
                  }),
                  React.createElement(YAxis, { 
                    domain: [0, 100], 
                    stroke: '#8892AB', 
                    fontSize: 10,
                    fontWeight: 500,
                    tickLine: false,
                    axisLine: false,
                    tickFormatter: (v) => v + '%'
                  }),
                  React.createElement(Tooltip, { content: React.createElement(CustomTooltip) }),
                  React.createElement(Legend, { 
                    iconType: 'circle',
                    iconSize: 8,
                    wrapperStyle: { fontSize: 11, fontWeight: 'bold', paddingTop: '10px' } 
                  }),
                  
                  selectedSubject === 'All' 
                    ? uniqueSubjects.map(sub => 
                        React.createElement(Line, {
                          key: sub,
                          type: 'monotone',
                          dataKey: sub,
                          name: sub,
                          stroke: subjectColors[sub] || '#0F2A5C',
                          strokeWidth: 3,
                          connectNulls: true,
                          activeDot: { r: 6, strokeWidth: 0 },
                          dot: { r: 3, strokeWidth: 0 }
                        })
                      )
                    : React.createElement(Line, {
                        type: 'monotone',
                        dataKey: 'score_pct',
                        name: selectedSubject,
                        stroke: subjectColors[selectedSubject] || '#0F2A5C',
                        strokeWidth: 3,
                        connectNulls: true,
                        activeDot: { r: 6, strokeWidth: 0 },
                        dot: { r: 4, strokeWidth: 0 }
                      })
                )
              )
            )
          );
        }

        const root = ReactDOM.createRoot(document.getElementById('app'));
        root.render(React.createElement(ChartApp));
      </script>
    </body>
    </html>
  `;

  return (
    <Card style={styles.container} padded={false}>
      <Text style={styles.chartTitle}>Performance Trend (Recharts)</Text>
      <View style={styles.webWrapper}>
        <WebView
          source={{ html: htmlContent }}
          style={styles.webview}
          originWhitelist={['*']}
          domStorageEnabled={true}
          javaScriptEnabled={true}
          scrollEnabled={false}
          showsVerticalScrollIndicator={false}
          showsHorizontalScrollIndicator={false}
        />
      </View>
    </Card>
  );
}

const styles = StyleSheet.create({
  container: {
    marginTop: Spacing.lg,
    overflow: 'hidden',
    backgroundColor: Colors.surface,
    paddingTop: Spacing.md,
    ...Shadows.card,
  },
  chartTitle: {
    fontSize: 15,
    fontWeight: '800',
    color: Colors.textPrimary,
    marginLeft: Spacing.lg,
    marginBottom: Spacing.xs,
  },
  webWrapper: {
    height: 290,
    width: '100%',
  },
  webview: {
    flex: 1,
    backgroundColor: 'transparent',
  },
  emptyContainer: {
    marginTop: Spacing.lg,
    height: 150,
    backgroundColor: Colors.surface,
    borderRadius: Radius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: Colors.border,
    borderStyle: 'dashed',
  },
  emptyText: {
    color: Colors.textMuted,
    fontSize: 14,
    fontWeight: '500',
  },
});
